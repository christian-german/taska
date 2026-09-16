Pour TaskService, ça fait un moment que je me dis qu'il faudrait séparer les tâches "classiques", des tâches récurrentes.
Pour ça, il faut bien comprendre que quand un client demande une liste de tâches entre deux dates, il reçoit une liste de DTO. Ces DTO peuvent représenter une tâche réélle (une instance de Task) ou une tâche dérivée d'une tâche récurrente (qui n'a qu'une instance en DB)

Je vais lire le cœur de la logique de récurrence avant de proposer quoi que ce soit.

Searched for 1 pattern, ran 3 shell commands

J'ai lu l'expansion, les cinq chemins de mutation et le modèle d'instance. Voici ce que je comprends du modèle, puis ce que j'en ferais.

Le modèle tel qu'il est

Une ligne tasks est en réalité deux choses différentes selon isRecurring :

- tâche ponctuelle — la ligne est la tâche. isCompleted, completedAt, scheduledAt la décrivent elle-même.
- définition de série — la ligne est un gabarit : scheduledAt sert de DTSTART, recurrenceRule de RRULE, rruleEndsAt de borne de troncature. Cette ligne n'est jamais « terminée » ; ses colonnes isCompleted / completedAt n'ont aucun sens pour elle.

Et il y a un troisième concept, qui n'a pas de ligne à lui : l'occurrence. Son identité est le couple (taskId, occurrenceScheduledAt), où occurrenceScheduledAt est l'instant produit par la RRULE. Elle est virtuelle tant qu'aucune ligne task_instances n'existe, matérialisée dès qu'on la termine, la saute ou la modifie.

Donc trois concepts, et un seul TaskDto pour les trois — c'est là qu'est la gêne que vous décrivez. Deux points qui le rendent concret :

Le gabarit est lui aussi renvoyé comme un TaskDto. findAll et findById renvoient la définition de série telle quelle, avec isRecurring = true, occurrenceScheduledAt = null. findOccurrencesForDateRange renvoie des occurrences. Le client doit déduire ce qu'il tient en inspectant occurrenceScheduledAt et isVirtual. Le même type transporte « un gabarit qui ne s'affiche nulle part dans un calendrier » et « une occurrence affichable ».

occurrenceScheduledAt et scheduledAt ne sont pas la même chose et ne peuvent pas fusionner. Le premier est l'identité de l'occurrence dans la série, le second est sa position réelle dans le temps. Une instance MODIFIED peut déplacer une occurrence hors de sa période naturelle — c'est exactement ce que gère la seconde requête findByTaskIdInAndStatusAndScheduledAtBetween avec son filtre. Toute simplification qui confondrait les deux casserait ce cas.

À quoi s'ajoute que FROM_THIS scinde une série en créant une nouvelle ligne tasks et en tronquant l'ancienne : une série au sens métier est donc potentiellement une chaîne de lignes, chacune avec ses propres instances.

Deux défauts trouvés en lisant

reopen détruit les personnalisations d'une occurrence. close charge l'instance existante — éventuellement MODIFIED avec un titre, une priorité, un horaire propres — et se contente de passer son statut à DONE : les surcharges survivent. reopen (TaskService.java:522) fait deleteByTaskIdAndOccurrenceScheduledAt. Donc : je modifie le titre d'une occurrence, je la coche, je la décoche → le titre personnalisé est perdu et l'occurrence redevient virtuelle. Il faudrait repasser à MODIFIED si des surcharges subsistent, et ne supprimer la ligne que si elle ne portait que le statut.

close et reopen ne traitent pas la même entrée de la même façon. Sur une tâche récurrente sans occurrenceScheduledAt, close rejette explicitement ; reopen tombe dans la branche « tâche ponctuelle » et écrit isCompleted = false sur le gabarit — une colonne qui ne veut rien dire pour lui. Silencieux, sans effet visible, mais c'est un état incohérent qu'on écrit en base.

Ce que je proposerais

La bonne nouvelle : le point de couture existe déjà. TaskResult est déjà une somme de deux cas, écrite avec des null :

public record TaskResult(Task task, TaskInstance taskInstance, Instant occurrenceScheduledAt) {
public static TaskResult base(Task task) { ... }
public static TaskResult occurrence(Task task, TaskInstance instance, Instant at) { ... }
}

Le transformer en type scellé rend la distinction vérifiable par le compilateur :

public sealed interface TaskResult permits OneOffTask, SeriesOccurrence {}
public record OneOffTask(Task task) implements TaskResult {}
public record SeriesOccurrence(Task series, TaskInstance instance, Instant occurrenceScheduledAt)
implements TaskResult {}

Ce que ça débloque, dans l'ordre de valeur :

1. TaskMapper.toDto(TaskResult) et toOccurrenceDto deviennent un switch sur le type au lieu d'une cascade de instance != null && instance.getX() != null ? ... : task.getX(). La règle « la surcharge gagne sur le gabarit » se dit une fois, à un endroit.
2. Chaque nouveau consommateur est obligé de traiter les deux cas — le compilateur refuse un switch incomplet sur un type scellé. C'est ce qui empêche la prochaine dérive du genre TaskOutput.
3. La frontière de découpe de TaskService devient évidente : ce qui produit et consomme OneOffTask d'un côté, SeriesOccurrence de l'autre. Les cinq if (!Boolean.TRUE.equals(task.getIsRecurring())) disséminés dans update, delete, close, reopen, replaceOccurrence se regroupent en un seul aiguillage en tête de chaîne.

Je ne toucherais pas à la persistance. Séparer Task en deux entités partagerait 90 % des colonnes, imposerait une migration et n'apporterait rien que le type scellé n'apporte déjà côté code.

La question qui commande le reste

Est-ce que la distinction doit remonter jusqu'au contrat d'API, ou rester interne ?

- Interne seule : TaskDto ne bouge pas, Android et le frontend ne bougent pas. On gagne la sûreté côté serveur et le découpage de TaskService, mais le client continue de deviner ce qu'il tient.
- Jusqu'au DTO : on expose la nature de l'objet (un discriminant, ou deux formes de réponse), et les champs instanceId / occurrenceScheduledAt / isVirtual cessent d'être nullables-selon-le-cas. C'est une rupture de contrat, à coordonner avec taska-android et taska-frontend.

Je commencerais par l'interne — c'est là qu'est l'essentiel du gain, et ça ne ferme pas la porte au second temps. Mais c'est votre appel, et il change le périmètre.