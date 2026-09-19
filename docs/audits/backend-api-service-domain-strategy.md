# Stratégie homogène pour les contrats API, les services et les règles métier du backend Taska

## Résumé exécutif

La stratégie recommandée repose sur des **paramètres applicatifs typés**, distincts des contrats HTTP et MCP.

Les services continuent à charger et manipuler des entités JPA managées, mais n'acceptent pas d'entité détachée comme représentation de la demande :

```java
Task update(
        UUID taskId,
        long expectedVersion,
        TaskUpdateParameters parameters,
        MutationActor actor);
```

plutôt que :

```java
Task update(UUID taskId, Task requestedTask);
```

Le coût est un record applicatif et un mapping par opération. Dans Taska, ce coût permet concrètement :

- de séparer création et mise à jour ;
- d'exclure structurellement les propriétés gérées par le serveur ;
- d'appliquer les mêmes validations métier depuis REST, MCP et un futur batch ;
- de distinguer sans ambiguïté les remplacements, actions et opérations de récurrence ;
- d'ajouter une vraie détection des mises à jour concurrentes ;
- de supprimer toute dépendance des services envers les DTO HTTP.

L'idée utile de l'approche fondée sur les entités est conservée : le service charge et modifie l'entité managée dans sa transaction. En revanche, l'entité JPA n'est pas utilisée comme contrat d'entrée du service.

## Périmètre et limites de l'audit

L'audit porte sur l'état local observé du dépôt. Au moment de l'analyse, la branche `master` contenait de nombreuses modifications non commitées touchant le backend, OpenAPI, OpenSpec et les clients. Cet état a été analysé tel quel, sans considérer qu'une modification locale constituait à elle seule une décision approuvée.

L'analyse est statique : aucun test, aucune implémentation et aucune opération GitHub n'ont été lancés. Les comportements de production, les données réelles, les accès concurrents et les consommateurs externes ne sont donc pas connus.

## État réel des parcours

| Parcours | Contrat d'entrée | Traitement réel | Effets |
|---|---|---|---|
| Création REST d'une tâche | `TaskRequest`, validé par `@Valid` | `TaskService.create`, valeurs par défaut, Inbox, validation du calendrier | `tasks_changed` publié par le controller |
| Édition REST complète | `TaskUpdateRequest`, tous les champs JSON requis | `TaskService.replace`, remplacement des propriétés mutables | reset notification si horaire changé, suppression de l'évaluation, publication REST |
| Planification | Aucun endpoint dédié pour une tâche normale | Les clients reconstruisent un PUT complet ; occurrence et série ont leurs endpoints propres | calendrier validé, notification parfois réarmée |
| Terminaison/réouverture | actions `close` et `reopen` | transition dans `TaskService`, différente pour tâche et occurrence | invalidation de priorité à la fermeture non récurrente, publication REST |
| Mutation MCP | `TaskUpdateInput` partiel | ancien `TaskService.update` et `applyPatch` | pas de publication `tasks_changed`, sémantique différente de REST |
| Batch futur | inexistant | aucune frontière définie | risque de dépendance directe aux repositories |
| Ressource simple : label | même `LabelRequest` pour POST et PUT | PUT partiel par champs non nuls | unicité laissée principalement à la base |

### Édition complète REST

Le contrat canonique exige actuellement le remplacement complet de toutes les propriétés mutables et exclut l'état serveur ([OpenSpec](../../openspec/specs/task-update-contract/spec.md), [OpenAPI](../openapi/schemas/task.yaml)). Jackson distingue l'omission d'un champ nullable de sa présence à `null` grâce à `@JsonProperty(required = true)` dans [TaskUpdateRequest.java](../../taska-backend/src/main/java/com/taska/domain/task/TaskUpdateRequest.java).

Le controller appelle directement `TaskService.replace` dans [TaskController.java](../../taska-backend/src/main/java/com/taska/domain/task/TaskController.java). Le service charge l'entité, applique tous les champs, invalide l'évaluation de priorité et renvoie lui-même un `TaskDto` dans [TaskService.java](../../taska-backend/src/main/java/com/taska/domain/task/TaskService.java).

C'est une vraie mise à jour complète, mais uniquement sur le chemin REST. La couche service reste dépendante du DTO HTTP et du mapper HTTP.

### Planification

Il n'existe pas de `scheduleTask` ou `rescheduleTask` applicatif dédié pour une tâche normale. Le web reçoit encore un `Partial<Task>`, recharge la tâche, fusionne les données puis envoie un PUT complet dans [task.service.ts](../../taska-frontend/src/app/core/services/task.service.ts). Android reconstruit un `TaskUpdateRequest` à partir d'un DTO en mémoire dans [TaskUpdateRequest.kt](../../taska-android/app/src/main/java/com/taska/android/data/model/TaskUpdateRequest.kt).

La règle du planning calendar est correctement centralisée dans `TaskService.assertScheduleAllowed`, appelée par la création, le remplacement complet et le remplacement d'occurrence. En revanche, l'effet local `isNotified=false` n'est centralisé que dans deux blocs de copie. Un changement de `allDay` sans changement de `scheduledAt` ne réarme pas la notification ; le comportement attendu n'est pas spécifié.

La planification est un bon candidat à une opération dédiée : elle possède déjà une règle externe, un effet serveur et plusieurs usages fonctionnels, dont le snooze dans [SnoozeReceiver.kt](../../taska-android/app/src/main/java/com/taska/android/SnoozeReceiver.kt).

### Terminaison et réouverture

Pour une tâche normale, `close` met à jour `isCompleted` et `completedAt`; `reopen` effectue l'inverse dans [TaskService.java](../../taska-backend/src/main/java/com/taska/domain/task/TaskService.java). Ces propriétés ne figurent pas dans `TaskUpdateRequest` : elles restent correctement sous contrôle du serveur.

Pour une occurrence récurrente, la terminaison transforme le `TaskInstance` en `DONE`. La réouverture supprime ensuite entièrement le `TaskInstance`. Une occurrence modifiée puis terminée perd donc ses modifications lorsqu'elle est rouverte. Les tests couvrent les opérations isolément, mais consacrent actuellement cette suppression dans [TaskServiceMutationTest.java](../../taska-backend/src/test/java/com/taska/domain/task/TaskServiceMutationTest.java).

Dans `TaskInstance`, `null` signifie également à la fois « hériter de la série » et « override explicitement effacé ». Le mapper retombe systématiquement sur la série lorsqu'un override vaut `null` dans [TaskMapper.java](../../taska-backend/src/main/java/com/taska/domain/task/TaskMapper.java). Le PUT d'une occurrence n'est donc pas réellement un remplacement pour les valeurs nullables.

### REST contre MCP

La divergence est constatée :

- REST utilise `replace` et un `TaskUpdateRequest` complet.
- MCP utilise encore `TaskService.update` et le patch partiel par non-null dans [TaskMcpTools.java](../../taska-backend/src/main/java/com/taska/mcp/TaskMcpTools.java).
- MCP duplique manuellement les validations du contenu, de la priorité et de l'estimation.
- Une priorité MCP non nulle est traitée comme absente sauf si `clearPriority=true`, car ce booléen est utilisé comme indicateur de présence.
- MCP ne publie jamais `tasks_changed`; cette publication est répétée uniquement dans les méthodes de [TaskController.java](../../taska-backend/src/main/java/com/taska/domain/task/TaskController.java).
- L'entrée et la sortie MCP de tâche omettent le `type`.
- MCP omet aussi `planningCalendarId` des projets dans [ProjectMcpTools.java](../../taska-backend/src/main/java/com/taska/mcp/ProjectMcpTools.java).

Cela contredit la promesse OpenSpec selon laquelle MCP délègue aux mêmes règles applicatives dans [taska-mcp-server/spec.md](../../openspec/specs/taska-mcp-server/spec.md).

### Ressource simple : label

Le label montre que la convention peut rester proportionnée :

- le même `LabelRequest` sert à créer et mettre à jour ;
- sa documentation dit que le nom est requis, mais le service traite `null` comme « ignorer » ;
- le controller de PUT n'emploie pas `@Valid` ;
- l'unicité est dans la base, tandis que `existsByName` n'est pas utilisé ;
- aucun test métier de `LabelService` n'existe.

Voir [LabelRequest.java](../../taska-backend/src/main/java/com/taska/domain/label/LabelRequest.java), [LabelController.java](../../taska-backend/src/main/java/com/taska/domain/label/LabelController.java) et [LabelService.java](../../taska-backend/src/main/java/com/taska/domain/label/LabelService.java).

La séparation proposée donnerait simplement :

```java
record LabelCreateRequest(
        String name,
        String color,
        Integer order,
        Boolean isFavorite) {}

record LabelUpdateRequest(
        String name,
        String color,
        int order,
        boolean isFavorite) {}

record LabelCreateParameters(
        String name,
        String color,
        int order,
        boolean favorite) {}

record LabelUpdateParameters(
        String name,
        String color,
        int order,
        boolean favorite) {}
```

La duplication nominale est faible et supprime l'ambiguïté entre création, remplacement et `null`.

## Localisation actuelle des responsabilités

| Responsabilité | Situation actuelle |
|---|---|
| Forme et complétude HTTP | Correcte pour `TaskUpdateRequest`; inégale ailleurs, notamment PUT label, projet et commentaire sans `@Valid` |
| Validation MCP | Dupliquée manuellement dans les tools |
| Invariants métier | Principalement dans les services, mais plusieurs invariants sont seulement documentés |
| Transitions d'état | Procédurales dans `TaskService`; entités à setters publics |
| Autorisation | Tout JWT valide accède au workspace partagé ; aucune autorisation par enregistrement |
| Transactions | `@Transactional` au niveau des services ; les mutations de `TaskService` sont donc atomiques en base |
| Chargement de l'entité | Généralement dans le service |
| Effets transactionnels | Suppression des évaluations dans `TaskService`; reset notification dans les routines de copie |
| Effets externes | Dans le controller REST, après la transaction; absents de MCP |
| Mappings | MapStruct dans certains controllers, mapping manuel dans MCP et `PlanningCalendarService`, `TaskMapper` appelé directement par `TaskService` |
| Protection concurrente | Aucune version JPA, aucun ETag ou `If-Match`, aucun `expectedVersion` |

L'authentification globale se trouve dans [WebSecurityConfiguration.java](../../taska-backend/src/main/java/com/taska/security/WebSecurityConfiguration.java). Le caractère volontairement partagé du workspace est explicitement spécifié pour MCP dans [taska-mcp-server/spec.md](../../openspec/specs/taska-mcp-server/spec.md). Il ne faut donc pas inventer une autorisation par propriétaire sans nouvelle décision fonctionnelle.

L'identité du compte reste néanmoins nécessaire pour l'effet `tasks_changed`, explicitement ciblé sur le compte ayant effectué la mutation dans [device-scoped-task-sync/spec.md](../../openspec/specs/device-scoped-task-sync/spec.md).

## Comparaison des deux stratégies

| Critère | Entité comme paramètre | Paramètres applicatifs |
|---|---|---|
| Mapping | Un mapping DTO vers entité | Un mapping DTO vers paramètres |
| Propriétés serveur | Doivent être ignorées par convention de mapper | Absentes du type, donc non assignables |
| Différence create/update | Cachée dans la même entité et ses valeurs par défaut | Exprimée par deux types |
| Champ absent contre `null` | Impossible à distinguer après construction | Le contrat complet interdit l'absence ; `null` reste une valeur |
| Réutilisation MCP/batch | Possible, mais chaque appelant peut fabriquer une entité partielle | Tous appellent le même service avec un contrat explicite |
| JPA | Mélange entité détachée demandée et entité managée chargée | Une seule entité : celle chargée dans la transaction |
| Récurrence | `Task` ne représente pas une occurrence virtuelle | Paramètres et résultat d'occurrence dédiés |
| Validation | Une entité construite directement peut contourner la complétude HTTP | Validation au service sur un type représentant exactement l'opération |
| Évolution | Ajouter un champ serveur agrandit implicitement l'entité d'entrée | Aucun impact sur l'entrée tant qu'il n'est pas client-modifiable |

Dans Taska, l'entité comme entrée pose quatre problèmes concrets :

1. `Task` contient `id`, `createdAt`, `updatedAt`, `completedAt`, `isNotified` et `rruleEndsAt`, qui ne doivent pas être contrôlés par le client dans [Task.java](../../taska-backend/src/main/java/com/taska/domain/task/Task.java).
2. Ses valeurs par défaut, notamment les booléens, masqueraient les champs absents.
3. La création et la mise à jour n'ont pas le même ensemble de champs ni les mêmes défauts.
4. Une occurrence virtuelle n'est pas une entité JPA.

Le record applicatif n'est donc pas une couche décorative : il définit la capacité accordée à l'appelant.

## Architecture cible recommandée

```text
HTTP XxxCreateRequest / XxxUpdateRequest     MCP XxxCreateInput / XxxUpdateInput
                    \                         /
                     \ mapping de frontière /
                      v                     v
                  XxxCreateParameters / XxxUpdateParameters
                                  |
                         @Transactional
                           XxxService
                    charge l'entité managée
                                  |
              validations externes + transitions métier
                                  |
                  entités / repositories / contraintes DB
                                  |
                       événement TaskChanged
                                  |
                 listener AFTER_COMMIT -> Firebase
```

### Controller REST

Le controller doit uniquement :

- désérialiser et valider la forme HTTP ;
- extraire le principal et le jeton de concurrence ;
- mapper la requête vers les paramètres applicatifs ;
- appeler le service ;
- mapper le résultat vers `XxxDto` ;
- laisser l'advice commun traduire les erreurs.

Il ne doit ni charger de repository, ni prendre une décision métier, ni publier lui-même un effet de mutation.

### Mapper de frontière

Le mapper réalise uniquement les conversions de frontière :

```java
TaskCreateParameters toParameters(TaskCreateRequest request);
TaskUpdateParameters toParameters(TaskUpdateRequest request);
TaskDto toDto(Task task);
TaskDto toDto(TaskOccurrence occurrence);
```

Le mapper REST n'est jamais utilisé par le service ou MCP. MCP possède son propre mapping vers les mêmes paramètres applicatifs. Un constructeur explicite est préférable à MapStruct lorsque le mapping est court ou comporte une décision visible.

### Service applicatif

Le service :

- porte `@Transactional` sur chaque cas d'usage mutateur ;
- charge l'entité managée dans la transaction ;
- vérifie version, existence, autorisation et références ;
- valide l'état cible complet ;
- appelle les transitions du domaine ;
- réalise les effets base de données connexes dans la même transaction ;
- enregistre l'événement de changement ;
- retourne une entité ou un résultat domaine, jamais un DTO HTTP.

Pour les lectures composées qui ne sont pas une entité, comme les occurrences virtuelles ou un calendrier avec ses règles, un résultat domaine immutable tel que `TaskOccurrence` ou `PlanningCalendarDetails` est justifié.

### Domaine

L'entité protège les invariants locaux et les effets inséparables de sa transition :

```java
void changeSchedule(Instant scheduledAt, boolean allDay) {
    boolean eligibilityChanged =
            !Objects.equals(this.scheduledAt, scheduledAt)
            || this.allDay != allDay;

    this.scheduledAt = scheduledAt;
    this.allDay = allDay;

    if (eligibilityChanged) {
        this.isNotified = false;
    }
}
```

Les setters des propriétés serveur ne devraient pas être publics. JPA utilise déjà l'accès direct aux champs et n'a pas besoin de tous ces setters publics.

Les règles exigeant d'autres agrégats, telles que le calendrier du projet, les cycles de projets ou l'existence du parent, restent dans le service ou dans une politique métier existante comme `PlanningCalendarService`.

### Repository

Le repository :

- charge et persiste ;
- peut fournir une variante verrouillée ou versionnée ;
- ne choisit pas la transition ;
- ne publie aucun effet ;
- conserve les contraintes de base comme dernier rempart.

Les controllers, MCP tools et batchs mutateurs ne doivent pas dépendre directement des repositories. Les accès directs actuels de `RegisterDeviceController` et des schedulers montrent que cette frontière n'est pas aujourd'hui générale.

## Garantie commune entre update complet et action dédiée

La garantie ne doit pas reposer sur deux implémentations similaires. Les deux opérations doivent appeler la même primitive interne ou la même méthode de domaine.

```java
@Transactional
public Task update(
        UUID taskId,
        long expectedVersion,
        TaskUpdateParameters requested,
        MutationActor actor) {

    Task task = loadAndCheckVersion(taskId, expectedVersion);

    validateTaskTargetState(requested);
    validateProjectAndParent(requested.projectId(), requested.parentId());

    task.changeContent(requested.content());
    task.changeType(requested.type());
    task.changeProject(requested.projectId());
    applySchedule(
            task,
            requested.projectId(),
            requested.scheduledAt(),
            requested.allDay());
    task.changeDeadline(requested.dueAt());
    // autres propriétés couvertes par le contrat

    priorityEvaluationRepository.deleteByTaskId(taskId);
    taskEvents.changedAfterCommit(actor.accountSubject());
    return task;
}

@Transactional
public Task reschedule(
        UUID taskId,
        long expectedVersion,
        TaskScheduleParameters requested,
        MutationActor actor) {

    Task task = loadAndCheckVersion(taskId, expectedVersion);

    applySchedule(
            task,
            task.getProjectId(),
            requested.scheduledAt(),
            requested.allDay());

    priorityEvaluationRepository.deleteByTaskId(taskId);
    taskEvents.changedAfterCommit(actor.accountSubject());
    return task;
}

private void applySchedule(
        Task task,
        UUID targetProjectId,
        Instant scheduledAt,
        boolean allDay) {

    assertScheduleAllowed(targetProjectId, scheduledAt, allDay);
    task.changeSchedule(scheduledAt, allDay);
}
```

Ce dispositif fournit trois niveaux de protection :

1. `Task.changeSchedule` garantit l'effet local, quel que soit l'appelant.
2. `applySchedule` garantit la règle dépendant du calendrier.
3. Les règles d'architecture interdisent les mutations directes depuis REST, MCP et batch.

Il reste une part de convention humaine lors de l'ajout d'une nouvelle méthode dans le service. Elle devient détectable par :

- des setters non publics ;
- des tests de parité entre update complet et action ;
- des règles ArchUnit interdisant les accès repository depuis les adaptateurs ;
- une revue exigeant qu'une propriété déjà couverte appelle sa primitive métier existante ;
- des contraintes PostgreSQL pour les invariants exprimables.

Ni Java ni Spring ne peuvent empêcher une migration SQL ou une requête native volontaire de contourner le domaine. Cette exception doit rester explicite.

### Effets externes

Le service doit enregistrer `TaskChanged(accountSubject)` dans la transaction, avec un listener `AFTER_COMMIT`. Cela n'introduit ni CQRS ni bus de commandes : il s'agit uniquement de déplacer un effet déjà existant du controller vers une frontière partagée.

Cela garantit :

- l'absence d'événement si la transaction est annulée ;
- le même événement pour REST, MCP et batch ;
- l'absence d'appel Firebase dans la transaction de base.

Cela ne garantit pas la livraison après un crash ou une panne Firebase. Une outbox ne serait justifiée que si cette durabilité devient une exigence fonctionnelle.

## Risque de concurrence

Le risque existe dans le modèle actuel :

1. le client lit la tâche en version A ;
2. une action dédiée ou un autre client modifie la planification ;
3. le premier client envoie son état complet A avec une autre modification ;
4. le PUT réécrit l'ancienne planification.

Le web réduit la fenêtre en rechargeant la tâche juste avant PUT, mais crée toujours une course GET/PUT. Android part souvent de son DTO en mémoire. Aucune entité ne possède `@Version`, et aucun contrat ne contient un jeton de concurrence.

Le fait que `isCompleted` soit exclu du PUT empêche un remplacement de rouvrir directement une tâche terminée. Il n'empêche pas l'écrasement de `scheduledAt`, `priority`, `order`, `projectId` ou des règles de calendrier.

La recommandation est d'ajouter une version persistée :

```java
@Version
private long version;
```

et un jeton client obligatoire pour toutes les mutations d'un agrégat :

- REST : ETag renvoyé, `If-Match` requis ;
- MCP : champ `expectedVersion` explicite ;
- batch : version lue puis transmise, conflit traité par abandon ou relecture ;
- service : vérification de `expectedVersion` et protection JPA au flush.

`@Version` seul ne suffit pas. Si le service charge l'état récent après réception d'un payload construit depuis une vieille lecture, JPA considère la mise à jour valide. Il faut transmettre la version observée par le client.

Pour REST, un `If-Match` périmé devrait normalement produire `412 Precondition Failed`. `409 Conflict` reste possible si un modèle uniforme hors HTTP est préféré.

## Convention déterministe pour les agents

### Contrats

1. Chaque ressource HTTP possède au minimum `XxxDto`, `XxxCreateRequest` et `XxxUpdateRequest`.
2. `XxxCreateRequest` contient uniquement les données acceptées à la création.
3. `XxxUpdateRequest` contient toutes les propriétés couvertes par le remplacement.
4. Chaque propriété de l'update est présente dans le JSON ; elle peut être nullable seulement si `null` a un sens métier explicite.
5. Un tableau vide signifie vider la collection. `null` ne signifie jamais « ignorer ».
6. Identifiants, timestamps, version calculée, état technique et propriétés de transition ne sont pas remplaçables.
7. Les propriétés inconnues sont rejetées.
8. Une action possède son propre contrat minimal : `TaskCloseRequest`, `TaskScheduleRequest`, etc.
9. Les contrats MCP restent propres à MCP, mais se mappent vers les mêmes paramètres applicatifs.

### Choix entre update complet et opération dédiée

Créer une opération dédiée si au moins une condition est satisfaite :

- c'est une transition nommée par le métier ;
- elle possède des états source autorisés ou interdits ;
- elle cible une occurrence, une série ou plusieurs ressources ;
- elle possède une identité, une portée, un résultat ou une idempotence propres ;
- elle déclenche des effets ayant une signification fonctionnelle ;
- elle évite de transporter une photographie complète uniquement pour changer un champ.

Sinon, la propriété reste modifiée par l'update complet.

Application à Taska :

- `complete/reopen` : opérations dédiées ;
- remplacement d'une occurrence : opération dédiée ;
- remplacement de la suite d'une série : opération dédiée ;
- suppression d'une occurrence ou troncature d'une série : opération dédiée ;
- réordonnancement de plusieurs projets ou tâches : opération dédiée ;
- planification, déplanification et snooze : opération dédiée recommandée ;
- contenu, description, type et estimation : update complet ;
- favori : envoyer explicitement la valeur cible, sans opération de toggle implicite.

Lorsqu'une propriété est accessible par les deux chemins, les deux appellent obligatoirement la même primitive métier, comme `applySchedule`. Une action dédiée ne possède pas sa propre copie de la règle.

### Services et transactions

1. Aucun service n'importe un DTO HTTP ou un input/output MCP.
2. Aucun service mutateur n'accepte une entité détachée comme demande.
3. Le service charge toujours l'entité managée.
4. L'intégralité du cas d'usage et de ses effets persistés est dans une transaction.
5. Toutes les validations portent sur l'état final, avant la première mutation irréversible.
6. Les effets externes sont déclenchés après commit.
7. Un batch qui modifie le métier appelle le même service ; il ne sauvegarde pas directement une entité.
8. Un scheduler peut utiliser directement un repository uniquement pour un état technique dont il est propriétaire, comme un curseur ou une livraison, pas pour contourner une transition de tâche.

## Migration progressive

### Étape 1 — Frontière applicative sans changement d'API

- Introduire `TaskCreateParameters`, `TaskUpdateParameters`, `OccurrenceUpdateParameters`, puis les équivalents projet, calendrier, label et commentaire.
- Conserver temporairement les contrats HTTP existants.
- Déplacer les mappings vers les controllers et MCP.
- Faire retourner aux services des entités ou résultats domaine.
- Déplacer `tasks_changed` hors du controller vers l'après-commit commun.
- Faire appeler par REST et MCP les mêmes méthodes applicatives.

Cette étape peut rester compatible extérieurement.

### Étape 2 — Normaliser les contrats HTTP

- Renommer `TaskRequest` en `TaskCreateRequest`.
- Retirer de la création `scope` et `occurrenceScheduledAt`, actuellement acceptés mais ignorés.
- Créer les paires create/update pour projet, label, commentaire et planning calendar.
- Supprimer `clearParent` : dans un update complet, `parentId:null` signifie clairement racine.
- Pour commentaire, limiter `CommentUpdateRequest` à `content`; la cible du commentaire est immuable.
- Pour calendrier, décider que `rules: []` efface les règles et que l'omission est invalide.
- Mettre `@Valid` sur tous les endpoints concernés et aligner OpenAPI.

C'est une rupture pour les PUT partiels existants hors tâche.

### Étape 3 — MCP

- Ajouter `type` aux tâches et `planningCalendarId` aux projets.
- Faire de `update_task` un remplacement complet, ou le déprécier au profit d'outils dédiés plus étroits.
- Ajouter les opérations récurrentes actuellement absentes ou imprécises.
- Supprimer `clearPriority`; une valeur nullable présente suffit dans un contrat complet.
- Extraire le sujet authentifié et le transmettre au service afin de publier `tasks_changed`.

Les outils MCP devront être versionnés ou migrés de manière coordonnée, car les schémas générés changeront.

### Étape 4 — Opération de planification

- Ajouter une opération applicative dédiée.
- Décider de l'exposition REST : sous-ressource `PUT/DELETE /tasks/{taskId}/schedule` ou action explicite.
- Migrer le snooze et les interactions qui ne changent que la planification.
- Garder `scheduledAt` dans l'update complet seulement si l'édition générale doit continuer à le couvrir.
- Faire converger les deux chemins sur `applySchedule`.

Cette étape supprime le GET préalable du service Angular et réduit les écrasements involontaires.

### Étape 5 — Concurrence

- Ajouter `version` aux agrégats mutables concernés.
- Exposer ETag ou version dans les lectures.
- Faire transmettre la version par web, Android, MCP et batch.
- Commencer en mode optionnel pour migrer les clients, puis rendre la précondition obligatoire.
- Ajouter une stratégie UX de conflit : recharger et demander à l'utilisateur, sans fusion silencieuse.

### Étape 6 — Récurrence et invariants

- Séparer l'état de complétion des overrides d'occurrence, ou ajouter une présence explicite par override nullable.
- Préserver les overrides lors d'un cycle modifier, terminer, rouvrir.
- Définir les invariants task, project, parent et recurrence.
- Ajouter les contraintes PostgreSQL exprimables.
- Migrer les données existantes seulement après avoir défini l'interprétation des `null` actuels.

## Vérifications ciblées

### Frontières structurelles

Des tests ArchUnit devraient vérifier que :

- les services ne dépendent pas de `*Dto`, `*CreateRequest`, `*UpdateRequest` ou du package MCP ;
- controllers, MCP tools et schedulers mutateurs ne dépendent pas des repositories métier ;
- les contrats HTTP ne sont utilisés que dans l'adaptateur HTTP ;
- les entités ne sont jamais des paramètres de controller ou de tool MCP ;
- les setters des propriétés serveur ne sont pas accessibles aux adaptateurs.

### Contrats

Pour chaque `XxxUpdateRequest` :

- omission d'un champ requis rejetée ;
- présence explicite à `null` acceptée uniquement si documentée ;
- propriété inconnue rejetée ;
- propriété serveur rejetée ;
- OpenAPI et binding Jackson vérifiés sur le même payload.

### Parité métier

À partir du même état initial, comparer :

- PUT complet modifiant la planification ;
- action dédiée de planification ;
- appel MCP correspondant ;
- appel batch correspondant.

Les quatre doivent produire le même :

- état final ;
- reset de notification ;
- invalidation d'évaluation ;
- événement après commit ;
- refus en cas de planning calendar incompatible ;
- conflit de version.

### Transitions de récurrence

Tester des séquences et non uniquement des méthodes isolées :

- virtuel vers modifié vers terminé vers rouvert ;
- occurrence modifiée avec override nullable explicite ;
- terminé vers suppression refusée ou définie ;
- occurrence invalide ;
- double terminaison ;
- deux créations concurrentes de la même instance ;
- double split concurrent d'une série.

### Transactions et effets

Avec PostgreSQL et Flyway réels, vérifier :

- aucune modification partielle si une validation tardive échoue ;
- nom et règles du planning calendar annulés ensemble ;
- split de série atomique ;
- suppression de l'évaluation atomique avec la tâche ;
- aucun événement `tasks_changed` après rollback ;
- un événement après commit pour REST et MCP ;
- conflit optimiste reproductible avec deux transactions.

Les tests actuels sont principalement fondés sur Mockito. Aucun test métier n'existe pour label, projet ou commentaire, et le test MCP d'intégration remplace les services par des mocks. Ils ne protègent donc ni les contraintes PostgreSQL, ni les transactions, ni la parité REST/MCP.

## Informations manquantes et décisions fonctionnelles

Les points suivants ne peuvent pas être décidés par l'architecture seule :

1. Quels invariants exacts relient `projectId` et `parentId` d'une tâche ?
2. Une tâche récurrente doit-elle obligatoirement posséder `scheduledAt` et `recurrenceRule` ?
3. `mentionContext` est-il encore modifiable après création, ou doit-il être limité à la création ou au serveur ?
4. Un changement de `allDay` à horaire identique doit-il réarmer une notification ?
5. Une occurrence explicitement déplanifiée doit-elle hériter d'un futur changement de la série ou rester déplanifiée ?
6. Réouvrir une occurrence modifiée doit-il préserver tous ses overrides ? C'est la recommandation de cet audit.
7. Le batch futur agit-il au nom d'un compte, de plusieurs comptes, ou sans notification utilisateur ?
8. La livraison de `tasks_changed` est-elle best-effort après commit, ou doit-elle survivre à un crash ?
9. Quels clients externes, hors web, Android et MCP local, dépendent encore des PUT partiels ?
10. Quelle expérience utilisateur doit gérer un conflit de version ?

## Arbitrages à valider avant toute implémentation

1. **Entrée des services** : paramètres applicatifs typés, entités uniquement managées en interne.
2. **MCP** : `update_task` complet, complété par des outils dédiés, sans patch générique.
3. **Planification** : ajout d'une opération dédiée utilisée par snooze et reschedule, tout en conservant ou non `scheduledAt` dans le PUT complet.
4. **Concurrence** : version obligatoire sur toutes les mutations, REST via `If-Match`; choix entre réponse `412` et `409`.
5. **Récurrence** : overrides nullables explicitement représentés et préservés lors de close et reopen.
6. **Ordre** : déterminer si `order` reste dans l'update complet ou devient exclusivement géré par une opération de réordonnancement.
7. **Propriétés client-modifiables** : confirmer notamment `mentionContext`, `projectId`, `parentId` et `isRecurring`.
8. **Acteur des mutations non REST** : définir comment MCP et un batch fournissent le sujet utilisé pour `tasks_changed`.
9. **Garantie de notification** : après-commit best-effort recommandé, ou outbox durable si la perte d'un événement est inacceptable.
10. **Règles métier manquantes** : définir la cohérence parent/projet, la validité d'une récurrence et les transitions autorisées d'une occurrence.
