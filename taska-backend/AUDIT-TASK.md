❯ Sans modifier le code, peux-tu me faire un bilan ici de ce qu'il reste à faire pour envisager la séparation, à minima dans le layer service, des deux notions tasks et occurrence. Et également, réfléchir à ces questions:
\- Les mappers devraient sortir de la couche controller

\- est-ce que "dueAt" sur les définition de tâches récurrentes a encore du sens?

- Renommer TaskInstance qui n'est pas représentatif de ce que c'est vraiment : Une modification d'une occurrence d'une série.

Je rassemble les faits avant de répondre — aucun fichier ne sera modifié.

Searched for 3 patterns, ran 4 shell commands

Aucun fichier modifié — uniquement de la lecture. Voici l'état des lieux.

1. Séparer tâches et occurrences dans le service

Ce qui est déjà acquis

La partie difficile est faite. TaskResult est scellé sur trois variantes, RecurringTaskOccurrenceResult porte la règle de résolution, TaskRecurrenceService isole déjà l'expansion RRULE, et getOrThrow est redevenu privé. Le compilateur sait désormais distinguer les trois notions ; il reste à ce que le service les traite séparément.

Ce qui reste

TaskService : 722 lignes, 14 méthodes publiques, 7 dépendances, 8 tests sur getIsRecurring(). Ces 14 méthodes se répartissent en quatre familles :

┌────────────────────┬─────────────────────────────────────────────────────────────────────┬───────────────────────────────────────────┐
│      Famille       │                              Méthodes                               │                  Nature                   │
├────────────────────┼─────────────────────────────────────────────────────────────────────┼───────────────────────────────────────────┤
│ Ligne tasks pure   │ findAll, findById, create, replace, getSubtasks, findTasksDueAround │ ne connaissent pas la notion d'occurrence │
├────────────────────┼─────────────────────────────────────────────────────────────────────┼───────────────────────────────────────────┤
│ Occurrence pure    │ findOccurrencesForDateRange, replaceOccurrence                      │ ne touchent jamais une tâche ponctuelle   │
├────────────────────┼─────────────────────────────────────────────────────────────────────┼───────────────────────────────────────────┤
│ Mixtes             │ update, delete, close, reopen                                       │ portent l'aiguillage en leur sein         │
├────────────────────┼─────────────────────────────────────────────────────────────────────┼───────────────────────────────────────────┤
│ Structure de série │ replaceFollowing                                                    │ tronque + clone une ligne                 │
└────────────────────┴─────────────────────────────────────────────────────────────────────┴───────────────────────────────────────────┘

Le travail tient dans les quatre méthodes mixtes. Elles ont toutes la même forme : if (pas récurrent || pas d'occurrence fournie) { chemin tâche } else { chemin occurrence }. C'est la forme d'un switch sur un type qui n'existe pas encore au moment de l'appel.

Le point d'aiguillage existe déjà

TaskMutationService est aujourd'hui un décorateur qui ne fait que publier une notification. C'est pourtant exactement le bon endroit pour l'aiguillage : il est indépendant du transport, il est déjà la frontière de mutation, et HTTP comme MCP passent par lui. Il charge la tâche, décide « ponctuelle / série / occurrence », et délègue à TaskService ou à un TaskOccurrenceService.

L'ordre que je suivrais

Étape 1 — Rendre les signatures explicites, sans changer de comportement. Aujourd'hui c'est la forme des paramètres qui décide : close(taskId, params) avec params.occurrenceScheduledAt() == null signifie « tâche », sinon « occurrence ». Tant que ce sera vrai, aucune séparation n'est possible, parce que le service ne peut trancher qu'après avoir lu ses arguments. Il faut passer à closeTask(taskId) / closeOccurrence(taskId, at), et de même pour reopen, delete, update. Mécanique, testable pas à pas, et c'est ce qui rend les trois étapes suivantes faciles. C'est le vrai préalable.

Étape 2 — Déplacer l'aiguillage dans TaskMutationService. Les 8 getIsRecurring() disparaissent de TaskService et deviennent un seul point de décision, en tête de chaîne.

Étape 3 — Extraire TaskOccurrenceService. Il récupère findOccurrencesForDateRange, replaceOccurrence, les branches occurrence de close/reopen/delete/update, validateOccurrence, et devient le seul utilisateur de TaskInstanceRepository et de TaskRecurrenceService. TaskService retombe autour de 350 lignes avec 4 dépendances.

Étape 4 — replaceFollowing et le FROM_THIS de update. Ces deux-là ne sont ni tâche ni occurrence : ils manipulent la structure d'une série (tronquer, cloner). Ils justifient peut-être un troisième service, RecurringSeriesService, ou restent dans TaskService comme opérations sur la définition.

Deux obstacles à anticiper

priorityEvaluationRepository.deleteByTaskId(taskId) est appelé sur 5 chemins de mutation. C'est un effet de bord transverse qui va se retrouver écartelé entre les deux services. Il devrait devenir un événement applicatif — le même mouvement que celui qui ferait disparaître TaskMutationService en tant que décorateur.

TaskServiceMutationTest fait environ 1 080 lignes et couvre les deux natures. Il se scindera selon la même ligne, mais c'est un volume à prévoir.

Ce que la séparation ferait tomber

Quatre défauts connus, tous symptômes de la confusion actuelle :

1. reopen détruit les personnalisations d'une occurrence modifiée (suppression de la ligne au lieu d'un retour à MODIFIED).
2. Asymétrie close/reopen : sur une série sans occurrence, close rejette, reopen écrit isCompleted = false sur le gabarit.
3. Le scheduler de notifications traite une série comme une tâche. findTasksDueAround n'a aucun filtre isRecurring, et TaskNotificationScheduler:59 fait task.setIsNotified(true) sur la ligne. Une tâche récurrente est donc notifiée une seule fois dans sa vie, à son DTSTART, puis plus jamais. C'est un vrai bug produit, pas une élégance de code.
4. TaskPriorityEvaluationService:74 se protège par un !isRecurring ad hoc. Avec des services séparés, cette garde devient structurelle au lieu d'être une condition qu'on peut oublier ailleurs.

---

2. Les mappers devraient-ils sortir de controller ?

Non — mais votre intuition pointe un vrai problème, qui n'est pas leur emplacement.

Un mapper existe pour traduire les types d'un transport donné. TaskMapper connaît TaskDto, TaskMcpMapper connaît TaskMcpOutput : aucun des deux n'est réutilisable ailleurs. Les sortir vers un package mapper recréerait une couche technique transverse aux features — exactement ce que l'organisation par feature refuse. Et depuis qu'on a remonté la résolution d'occurrence dans RecurringTaskOccurrenceResult, ils sont devenus fins : de la recopie de champs et des @Mapping. Il n'y a plus de logique à en extraire.

Ce qui gêne réellement, ce sont deux dépendances croisées entre features :

ProjectController  →  TaskMapper                    (project → controller de task)
TaskController     →  TaskPriorityEvaluationMapper  (task → controller de priority)

Et dans les deux cas, la dépendance existe pour compenser un défaut de découpage, pas parce qu'un mapper serait mal placé :

- GET /projects/{id}/tasks fait doublon avec GET /tasks?projectId=, qui existe déjà et fonctionne. L'endpoint redondant est la seule raison pour laquelle ProjectController a besoin de projeter des tâches. Le supprimer élimine la dépendance sans rien déplacer.
- La feature priority n'a pas de contrôleur ; son unique endpoint est greffé sur TaskController. Lui donner son propre TaskPriorityEvaluationController — en gardant la même URL /tasks/{taskId}/priority-evaluation — supprime la seconde dépendance et complète une feature aujourd'hui amputée.

Donc : garder les mappers où ils sont, et retirer les deux raisons qu'ont les features de se traverser.

---

3. dueAt sur une définition récurrente

Non, il n'a plus de sens — et le code le dit déjà plus fort que je ne le pensais.

Le constat factuel d'abord : aucune requête du dépôt ne filtre sur due_at. Les trois requêtes de période filtrent t.scheduledAt, et findTasksDueAround aussi, malgré son nom. Pire, les Javadoc de findNonRecurringTasksInPeriod et de sa variante annoncent « whose due date falls within [start, end) » alors que le JPQL lit scheduledAt. Tout le vocabulaire « due » du dépôt est faux : un nom de méthode et trois commentaires. dueAt n'est aujourd'hui qu'un champ stocké, recopié et affiché — il ne pilote aucune requête, aucune notification, aucun tri.

Sur le fond ensuite : sur une série, dueAt est un instant absolu unique hérité par toutes les occurrences. Une tâche quotidienne dont dueAt vaut le 20 mai 2026 donne à l'occurrence de mars 2027 une échéance située dix mois dans le passé. Ce n'est pas discutable, c'est simplement dénué de sens.

Au niveau de l'instance, en revanche, dueAt est parfaitement légitime : « cette occurrence-ci est due à telle heure ». C'est une surcharge ponctuelle, pas un gabarit.

Trois issues possibles :

- Retirer dueAt de la définition de série. RecurringTaskSeriesDto le perd, resolvedDueAt() se réduit à l'instance seule. Honnête, et c'est un champ qui ne sert déjà à rien. Coût : rupture de contrat, donc à grouper avec un autre changement client.
- Le réinterpréter en décalage : dueAfter: Duration sur la série, résolu par occurrence (occurrenceScheduledAt + dueAfter). C'est la sémantique qu'on attend vraiment d'une échéance récurrente, mais c'est un changement de type et un nouveau champ à faire vivre dans les trois clients.
- Le garder comme valeur d'affichage par défaut, en assumant.

Je ferais la première maintenant et la seconde le jour où le produit demande des échéances sur les occurrences. Indépendamment du choix : le vocabulaire « due » du dépôt est à corriger dès maintenant, il induit en erreur quiconque lit ces requêtes.

---

4. Renommer TaskInstance

D'accord sur le principe, avec une nuance sur la définition.

Vous le décrivez comme « une modification d'une occurrence d'une série ». C'est vrai pour le statut MODIFIED, mais la ligne porte aussi DONE et SKIPPED — terminer ou sauter une occurrence n'est pas la modifier. Ce que la ligne matérialise, c'est l'écart entre ce que la RRULE produirait seule et ce qui s'est réellement passé pour cette occurrence : elle a été faite, sautée, ou altérée.

Le code a déjà le bon mot pour son contraire : virtual(). La phrase qui doit rester vraie après le renommage est « une occurrence est virtuelle tant qu'il n'existe pas de X ».

┌────────────────────────┬───────────────────────────────────────────────────────────┐
│        Candidat        │                          Verdict                          │
├────────────────────────┼───────────────────────────────────────────────────────────┤
│ OccurrenceOverride     │ juste pour MODIFIED, faux pour DONE et SKIPPED            │
├────────────────────────┼───────────────────────────────────────────────────────────┤
│ PersistedOccurrence    │ s'oppose exactement à virtual(), couvre les trois statuts │
├────────────────────────┼───────────────────────────────────────────────────────────┤
│ OccurrenceRecord       │ neutre et correct, un peu pâle                            │
├────────────────────────┼───────────────────────────────────────────────────────────┤
│ MaterializedOccurrence │ précis mais jargonneux                                    │
└────────────────────────┴───────────────────────────────────────────────────────────┘

Ma préférence va à PersistedOccurrence, avec TaskInstanceStatus → OccurrenceStatus.

Deux renommages vont plus loin que le nom de la classe :

taskId sur cette entité désigne toujours une série, jamais une tâche ponctuelle — seriesId dirait la vérité et rendrait la contrainte évidente à la lecture.

instanceId, en revanche, est exposé dans RecurringTaskOccurrenceDto : le renommer touche le contrat et les deux clients. À grouper avec le changement sur dueAt plutôt qu'à faire seul.

Bonne nouvelle sur le coût : en gardant @Table(name = "task_instances"), le renommage Java ne demande aucune migration de base. On peut donc le faire tout de suite et laisser le nom de table diverger, ou aligner la table plus tard dans une migration dédiée.

---

Ce que je ferais dans quel ordre

Le renommage de TaskInstance est indolore et améliore la lisibilité de tout ce qui suit — je le passerais en premier. Les deux défauts close/reopen ensuite, puisque vous vouliez les traiter maintenant et qu'ils sont petits. Puis l'étape 1 de la séparation, qui est le vrai verrou. Le bug de notification des récurrentes mérite d'être traité indépendamment et sans attendre : c'est le seul de la liste qui soit visible par un utilisateur.