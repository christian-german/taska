2. Trois idiomes de mapping coexistent.
- MapStruct réel : LabelMapper, ProjectMapper, CommentMapper, TaskMapper
- @Mapper avec uniquement des default écrits à la main — MapStruct n'apporte strictement rien : PlanningCalendarMapper
- Factory statique sur le DTO, avec un @Mapper qui ne fait que déléguer : TaskPriorityEvaluationDto.from() + TaskPriorityEvaluationMapper

3. Le package mcp/ duplique toute la couche de présentation de task, et la duplication a déjà divergé. TaskOutput.from() (mcp/TaskMcpTools.java:378) réimplémente à la main la même logique que TaskMapper.toOccurrenceDto() (controller/TaskMapper.java:66) — et TaskOutput n'a pas le champ type que TaskDto a gagné. La feature task est donc éclatée sur deux packages racine. Soit mcp descend en domain/task/mcp/ (cohérent avec « feature d'abord »), soit controller remonte à côté de mcp comme deux adaptateurs frères.

4. ~~Gestion d'erreur à deux vitesses~~ Résolu : LabelExceptionHandler supprimé (usage mono-utilisateur, race condition sur le nom de label non pertinente ; DataIntegrityViolationException tombe désormais sur le catch-all comme pour Project.name / DeviceToken.token, comportement homogène entre domaines).

5. @JsonAnySetter rejectUnknownProperty n'existe que sur les 2 records de Label. C'est une règle de contrat qui devrait être une config Jackson globale (FAIL_ON_UNKNOWN_PROPERTIES), pas 10 lignes copiées par record.

6. Vocabulaire non stabilisé. update vs replace vs replaceFollowing vs replaceOccurrence ; PlanningCalendarService.get() vs findById() ailleurs ; et surtout TaskUpdateParameters est consommé par replace() tandis que TaskPatchParameters est consommé par update() — le nom du record ne correspond pas à la méthode.

7. Feature sans la pile complète. priority n'a ni Controller ni Request ni Parameters (ses endpoints sont greffés sur TaskController), notification passe un String token brut au service. Si la règle Request→Parameters est un standard, elle a des exceptions non documentées ; si c'est du cas par cas, alors les 4 champs strictement identiques de LabelCreateRequest/LabelCreateParameters sont de la cérémonie pure.

Les points structurels plus lourds

TaskService : 739 lignes, 7 dépendances, 16 méthodes publiques. C'est la classe fourre-tout classique. Elle mélange CRUD, expansion de récurrence, validation de planning calendar et lecture des évaluations de priorité. getOrThrow() y est public — un détail interne exposé. Il y a matière à extraire au moins TaskOccurrenceService (les replaceOccurrence/close/reopen/validateOccurrence) et à sortir assertScheduleAllowed vers un validateur dédié.

TaskMutationService est un décorateur écrit à la main. Ses 8 méthodes ne font que taskService.X(...) puis publishChange(subject). Un ApplicationEventPublisher + @TransactionalEventListener dans notification supprimerait la classe entière — et au passage corrigerait le fait qu'aujourd'hui la notification part même si la transaction rollback.

Les constructeurs de « compatibilité ». TaskDto (ligne 78), TaskCreateInput, TaskUpdateInput ont chacun un constructeur surchargé « for callers compiled before task type was introduced ». Dans un monolithe recompilé à chaque build, ça ne protège rien : ce sont juste des chemins non testés qui injectent silencieusement TaskType.TODO et dueAt = null.

Fuites entre features au niveau adaptateur. ProjectController injecte TaskMapper, TaskController injecte TaskPriorityEvaluationService + TaskPriorityEvaluationMapper. Ce n'est pas dramatique, mais ça veut dire que les frontières de feature ne tiennent qu'au niveau service.

@JsonProperty(required = true) sur les records *UpdateRequest. À vérifier par un test : historiquement Jackson n'applique pas required à la désérialisation (c'est de la génération de schéma). Si c'est le cas ici, l'intention « champ obligatoirement présent » n'est portée que par les @NotNull, et content/description/projectId annotés required mais sans @NotNull ne sont en fait pas contrôlés.

Si je devais prioriser

1. Réparer l'import DeviceToken (le code ne compile pas en l'état) et fixer une règle unique pour l'emplacement des entités.
2. Remonter le rejet des propriétés inconnues au niveau global.
3. Choisir un seul idiome de mapping et aligner PlanningCalendarMapper + TaskPriorityEvaluationMapper.
4. Faire partager à mcp et controller le même mapping de sortie (ou au minimum resynchroniser TaskOutput avec TaskDto).
5. Supprimer les constructeurs de compatibilité.
6. Remplacer TaskMutationService par un événement applicatif, puis découper TaskService.

Les points 1 à 5 sont mécaniques et à faible risque. Le 6 mérite un commit dédié.