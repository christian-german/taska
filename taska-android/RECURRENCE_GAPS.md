# Non-conformités vis-à-vis du prompt — Tâches récurrentes Android

Ce document recense chaque règle du prompt initial qui n'est pas respectée dans le code actuel,
qu'il s'agisse de comportements applicatifs ou de règles d'écriture des tests.

---

## 1. Lacunes architecturales

### 1.1 Absence de `TaskRepository`

**Règle (prompt §"Classes à tester") :**
> `TaskRepository` — Appels Retrofit + mapping des réponses

**Réalité :** Il n'existe pas de couche Repository. Les ViewModels (`TodayViewModel`,
`TaskDetailViewModel`, etc.) appellent `RetrofitClient.api` directement. Toute la logique
d'accès réseau est inline dans les ViewModels.

**Impact :** Les cas 2.1 à 2.12 ne peuvent pas être testés en isolation. Les tests unitaires
des ViewModels testent indirectement le comportement réseau, sans découplage.

---

### 1.2 Absence de `TaskDtoMapper`

**Règle (prompt §"Classes à tester") :**
> `TaskDtoMapper` — Mapping `TaskDto` → modèle domaine

**Réalité :** Il n'y a pas de mapper. `TaskDto` est utilisé directement comme modèle domaine
dans toute l'application. Le backend retourne un DTO déjà fusionné (parent + overrides
de l'instance), et le client l'utilise tel quel.

**Conséquence sur le cas 1.4 :** La règle
> *"Champs de l'instance (content, priority) prioritaires sur le parent"*

n'a pas de traduction côté Android : il n'y a pas de logique de merge côté client.
C'est le backend qui est responsable de cette priorité. Le cas 1.4 ne peut donc pas être
testé unitairement côté Android.

---

### 1.3 Absence de `RecurrenceActionHandler`

**Règle (prompt §"Classes à tester") :**
> `RecurrenceActionHandler` (si existant) — Logique de routing selon `isRecurring`

**Réalité :** La logique de routing est inline dans `TodayViewModel.requestDeleteTask()`.
Elle n'est ni extraite dans une classe dédiée, ni réutilisable depuis d'autres ViewModels
(`DayViewModel`, `WeekViewModel`).

---

## 2. Comportements applicatifs non implémentés

### 2.1 Dialog de scope manquant pour la complétion — cas 3.2, 3.3, 3.5

**Règle (prompt §3a) :**
> **3.2** — Tâche récurrente : appel `closeTask` → Émet un événement `ShowRecurrenceScopeDialog`
> **avant** d'appeler le repository
>
> **3.3** — Dialog confirme `THIS_ONLY` → Appelle `repository.closeTask(id, scheduledAt)`
>
> **3.5** — Dialog annulé → Aucun appel repository, état UI inchangé

**Réalité (`TodayViewModel.closeTask`) :**

```kotlin
fun closeTask(task: TaskDto) {
    viewModelScope.launch {
        val body = CloseReopenRequest(scheduledAt = task.scheduledAt)
        val closed = RetrofitClient.api.closeTask(task.id, body)
        // ...
    }
}
```

La complétion appelle directement l'API sans afficher de dialog, quelle que soit la valeur
de `isRecurring`. Il n'y a pas de mécanisme d'annulation possible par l'utilisateur avant
l'appel réseau.

---

### 2.2 Modification avec scope non implémentée — cas 3.8 à 3.14

**Règle (prompt §3b) :**

| Cas | Attendu |
|-----|---------|
| 3.9 | Tâche récurrente → émet `ShowRecurrenceScopeDialog` |
| 3.10 | THIS_ONLY → `repository.updateTask` avec `scope=THIS_ONLY` et `scheduledAt` |
| 3.11 | FROM_THIS → `repository.updateTask` avec `scope=FROM_THIS` et `scheduledAt` |
| 3.12 | Dialog annulé → aucun appel repository |
| 3.13 | THIS_ONLY succès → occurrence mise à jour dans la liste |
| 3.14 | FROM_THIS succès → la liste est rechargée |

**Réalité (`TaskDetailViewModel.applyUpdate`) :**

```kotlin
private fun applyUpdate(transform: TaskRequest.() -> TaskRequest) {
    val base = TaskRequest(content = task.content, ...)
    viewModelScope.launch {
        val updated = RetrofitClient.api.updateTask(taskId, base.transform())
        // ...
    }
}
```

`updateTask` est toujours appelé sans `scope` ni `scheduledAt`, quelle que soit la nature
de la tâche (récurrente ou non). Aucun dialog de scope n'est déclenché. La `TaskRequest`
envoie systématiquement `scope = null` et `scheduledAt = null`.

---

### 2.3 Masquage optimiste après suppression THIS_ONLY — cas 3.20

**Règle (prompt §3c) :**
> **3.20** — Delete succès `THIS_ONLY` → Occurrence retirée de la liste locale
> **(masquage optimiste)**

**Réalité (`TodayViewModel.confirmDeleteTask`) :**

```kotlin
fun confirmDeleteTask(task: TaskDto, scope: RecurrenceScope?) {
    viewModelScope.launch {
        RetrofitClient.api.deleteTask(task.id, body)
        load() // recharge depuis l'API dans tous les cas
    }
}
```

Après une suppression `THIS_ONLY`, `load()` est appelé (rechargement complet depuis l'API),
y compris pour les suppressions `FROM_THIS`. La liste n'est jamais mise à jour localement
de façon optimiste : l'occurrence n'est pas retirée avant la réponse du serveur.

---

### 2.4 Cas 1.7 — `scheduledAt` null sur récurrent ne lève pas d'erreur

**Règle (prompt §1) :**
> **1.7** — `scheduledAt` null sur une tâche `isRecurring=true` → Lève une exception de
> mapping ou retourne un état d'erreur

**Réalité (`TodayViewModel.requestDeleteTask`) :**

```kotlin
fun requestDeleteTask(task: TaskDto) {
    if (task.isRecurring == true && task.scheduledAt != null) {
        _uiState.update { it.copy(pendingDeleteTask = task) }
    } else {
        confirmDeleteTask(task, scope = null) // traité silencieusement comme non-récurrent
    }
}
```

Quand `isRecurring=true` et `scheduledAt=null`, la tâche est traitée silencieusement comme
une tâche non-récurrente. Aucune erreur n'est levée, aucun état d'erreur n'est exposé à
l'UI. Ce comportement masque une incohérence dans les données reçues du backend.

---

### 2.5 Cas 5.1 — Pas de protection contre le double-tap

**Règle (prompt §5) :**
> **5.1** — Double tap rapide sur "Terminer" (appel concurrent) → Deuxième appel ignoré
> (debounce ou guard)

**Réalité :** Aucun mécanisme de debounce ni de garde n'existe dans `closeTask()`,
`requestDeleteTask()` ou `confirmDeleteTask()`. Deux appels rapides successifs envoient
deux requêtes réseau indépendantes.

---

### 2.6 Cas 5.2 — Aucune politique de rejet pendant un chargement en cours

**Règle (prompt §5) :**
> **5.2** — Action lancée pendant un chargement en cours → Action mise en file ou rejetée
> selon politique définie

**Réalité :** `isLoading` est bien exposé dans `TodayUiState`, mais aucune action (close,
delete, reopen) ne vérifie cet état avant de lancer un appel réseau. Il est possible de
déclencher plusieurs actions concurrentes pendant que `load()` est en cours.

---

## 3. Règles d'écriture des tests non respectées

### 3.1 `confirmVerified()` absent

**Règle (prompt §"Instructions", point 6) :**
> Utilise `confirmVerified()` pour s'assurer qu'aucun appel inattendu n'a eu lieu.

**Réalité :** `confirmVerified(api)` n'est utilisé dans aucun test du fichier
`TodayViewModelTest.kt`. Cela signifie qu'un appel inattendu à l'API (ex. un `getTasks`
supplémentaire non prévu) ne ferait pas échouer le test.

**Exemple de test concerné :** `givenPendingDelete_whenDismissDeleteScope_thenPendingClearedNoApiCall`
vérifie avec `coVerify(exactly = 0)` qu'il n'y a pas eu de `deleteTask`, mais ne garantit
pas l'absence d'autres appels API non prévus.

---

### 3.2 Turbine absent des dépendances

**Règle (prompt §"Instructions", point 2 et point 9) :**
> `turbine` si des `Flow` sont exposés par les ViewModels
>
> **9.** Dialog events : si le ViewModel expose les événements via un `SharedFlow` ou
> `Channel`, utilise `turbine` pour les collecter et les vérifier.

**Réalité :** `turbine` n'a pas été ajouté aux dépendances (`build.gradle.kts`). Les
événements de dialog sont dans `StateFlow<TodayUiState>` (pas dans un `SharedFlow` ni un
`Channel`), ce qui rend turbine non strictement obligatoire ici. Cependant, si des
événements one-shot (navigation, snackbar) étaient ajoutés via `SharedFlow`, les tests
actuels ne seraient pas équipés pour les capturer correctement.

---

### 3.3 Tests manquants pour les fonctionnalités non implémentées

**Règle (prompt §"Cas à couvrir obligatoirement") :**
Les cas suivants sont listés comme obligatoires mais n'ont pas de tests, car les
fonctionnalités correspondantes ne sont pas implémentées :

| Cas | Description | Raison de l'absence |
|-----|-------------|---------------------|
| 3.2 | Dialog scope sur `closeTask` récurrent | Dialog non implémenté pour close |
| 3.3 | Close THIS_ONLY via dialog | Dialog non implémenté pour close |
| 3.5 | Annulation du dialog de close | Dialog non implémenté pour close |
| 3.9 | Dialog scope sur `updateTask` récurrent | Update sans scope dans le code |
| 3.10 | Update THIS_ONLY | Update sans scope dans le code |
| 3.11 | Update FROM_THIS | Update sans scope dans le code |
| 3.12 | Annulation du dialog d'update | Update sans scope dans le code |
| 3.13 | Update THIS_ONLY → occurrence mise à jour | Update sans scope dans le code |
| 3.14 | Update FROM_THIS → liste rechargée | Update sans scope dans le code |
| 3.20 | THIS_ONLY delete → suppression optimiste locale | Reload complet dans le code |

---

### 3.4 `TaskRepositoryTest.kt` non créé

**Règle (prompt §"Instructions", point 3) :**
> Un fichier par classe testée : `TaskRepositoryTest.kt`

**Réalité :** Le fichier n'a pas été créé parce que `TaskRepository` n'existe pas dans le
projet. Les tests d'interaction avec l'API sont couverts indirectement dans
`TodayViewModelTest.kt`, mais il n'y a pas de fichier dédié au contrat de l'API.

---

## Résumé

| Catégorie | Règle | Statut |
|-----------|-------|--------|
| Architecture | Repository pattern | ✗ Absent |
| Architecture | Mapper DTO → domaine | ✗ Absent |
| Architecture | Handler dédié recurrence | ✗ Absent |
| Comportement | Dialog scope sur close | ✗ Non implémenté |
| Comportement | Update avec scope (THIS_ONLY / FROM_THIS) | ✗ Non implémenté |
| Comportement | Suppression optimiste après THIS_ONLY | ✗ Non implémenté |
| Comportement | Erreur si `scheduledAt` null sur récurrent | ✗ Non levée |
| Comportement | Debounce double-tap | ✗ Non implémenté |
| Comportement | Garde pendant chargement | ✗ Non implémenté |
| Tests | `confirmVerified()` | ✗ Non utilisé |
| Tests | Turbine en dépendance | ✗ Non ajouté |
| Tests | Tests des features manquantes | ✗ Impossible sans implémentation |
| Tests | `TaskRepositoryTest.kt` | ✗ Impossible sans Repository |
