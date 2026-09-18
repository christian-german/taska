# Résumé de la journée

1. Le point de départ : votre reproduction

Vous avez créé une série quotidienne, marqué une occurrence future à DONE, puis supprimé la récurrence depuis l'occurrence du jour. Résultat : une ligne is_recurring = true avec recurrence_rule = NULL.

L'analyse a fait apparaître trois défauts distincts dans un seul parcours :

┌───────────────────────────────────────────────────────────┬──────────────────────────────────────────────────────────────┐
│                          Défaut                           │                            Nature                            │
├───────────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────────┤
│ Invariant is_recurring ⇒ recurrence_rule non vérifié      │ ni validation applicative, ni contrainte en base             │
├───────────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────────┤
│ Aucune opération « arrêter la récurrence » sur /following │ l'endpoint rejette isRecurring: false, le client a contourné │
├───────────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────────┤
│ États d'occurrence orphelins au-delà d'une troncature     │ aucun des trois chemins de troncature ne nettoyait           │
└───────────────────────────────────────────────────────────┴──────────────────────────────────────────────────────────────┘

Deux conséquences non évidentes sont apparues à la lecture du code : la ligne corrompue tue toute la campagne de notifications (l'expansion lève hors du @Scheduled, sans try/catch), et les orphelins ne sont pas dormants — la boucle « moved in » affiche les MODIFIED orphelins dans le calendrier.

2. Les décisions, et pourquoi

La règle de récurrence n'est jamais rétroactive

Modifier la règle en place recalcule les occurrences passées depuis le DTSTART. Un passage de quotidien à hebdomadaire rend inatteignables tous les DONE posés sur des jours que la nouvelle règle ne génère plus : on affirmerait rétroactivement un rythme qui n'a pas eu lieu. Le passé est un fait, pas un paramètre.

Exception conservée : l'édition en place quand la série n'a aucun état — il n'y a rien à préserver, et c'est le cas fréquent de la correction juste après création.

Plus de choix de portée pour la récurrence

Conséquence directe de la précédente : si un changement de règle s'applique toujours « à partir de maintenant », l'utilisateur n'a plus rien à arbitrer. La question « cette occurrence / toute la suite » disparaît pour la récurrence.

Ancre et règle sont deux opérations différentes

Le piège qui brouillait toute la discussion : « décaler l'horaire » désigne deux choses. Sur une occurrence, c'est une ancre MODIFIED — votre exemple du cours de guitare, mécanisme inchangé. Sur la série, l'horaire n'est pas dans la RRULE mais dans scheduled_at, qui sert de DTSTART : le modifier régénère tous les instants, passés compris. C'est donc un paramètre générateur, et ça tronque.

Le flag detached — la décision pivot

C'est votre proposition, et elle a dissous le problème au lieu de l'arbitrer. Une ancre orpheline garde un sens : un DONE est un fait daté, un MODIFIED un placement délibéré. La garder affichable a supprimé trois règles d'un coup — le repoussement de la frontière, l'alerte de perte, et le transfert des ancres vers le successeur.

SKIPPED fait exception et reste supprimé : un saut est une absence, sans objet une fois l'occurrence disparue. Il n'y a rien à afficher.

Flag stocké plutôt que dérivé

Dérivé serait toujours exact et permettrait le réattachement si la règle revenait en arrière. Mais les lectures dominent — l'expansion tourne à chaque vue jour et semaine — alors que le détachement ne survient qu'au changement de règle, c'est-à-dire dans l'opération qui fait déjà le travail de troncature. Contrepartie assumée : pas de réattachement automatique.

Ce qui a été écarté, et pourquoi

Le modèle à périodes de validité (une ligne tasks, un historique de règles) : il ne règle pas le problème d'ancre, puisque celui-ci naît du placement de la frontière, pas de la forme de stockage — votre exemple des courses produit exactement la même orpheline dans ce modèle. Il ne règle que la fragmentation d'id, et déplace la complexité vers le chemin de lecture le plus chaud. Le compromis est l'inverse de l'intuition : l'option qui garde un seul id est la plus coûteuse à lire.

La matérialisation des orphelines en tâches ponctuelles : même résultat visible, chemin de lecture inchangé, mais perte du lien à la série — précisément ce qu'on cherchait à préserver.

La fragmentation d'id est assumée, series_root_id reporté. Coût connu et accepté : commentaires et sous-tâches ne suivent pas le successeur.

Sur TaskOccurrenceState

Confirmé que le DONE futur reste autorisé — « faire les courses la veille » est un usage normal, pas un cas marginal. C'est ce qui a donné du poids à votre exemple et fait émerger le flag.

3. Ce qui a été livré

Lot 1 — les garde-fous. Invariant is_recurring ⇒ recurrence_rule aux quatre chemins d'écriture, doublé d'un CHECK en base (V26) et d'une migration qui bascule les lignes corrompues en non récurrentes. Filtre sur la règle dans les candidats à notification, plus isolation par série pour qu'une règle invalide ne coûte plus ses notifications à tout le monde. Boucle « moved in » bornée par la portée réelle de la série.

Lot 2 — les ancres détachées. Colonne detached (V27), marquage aux trois points de troncature, SKIPPED supprimés, lecture par une requête non adossée aux séries actives — c'était le point délicat, une ancre détachée survit à la portée de sa série. Exposition dans les deux contrats, HTTP et MCP, et dans l'OpenAPI. Alignement de updateSeriesFrom sur replaceFollowing pour le point de départ du successeur.

La migration V27 traite aussi les orphelins déjà en base : votre occurrence terminée devrait réapparaître au déploiement.

205 tests, contre 192 au début de la journée.

4. Ce qui reste ouvert

Le lot 3 : mise à jour des deux clients pour isDetached, décision d'affichage d'une occurrence détachée, retitrage du dialogue générique — il s'affiche aujourd'hui pour n'importe quel champ sous le titre « Modifier la récurrence » — et le renommage en « Arrêter la série ».

Deux décisions volontairement différées : series_root_id et la copie des commentaires et sous-tâches vers le successeur.

Et un défaut de conception relevé mais non traité : /following refuse toujours isRecurring: false, donc « arrêter la série » n'a pas d'expression légitime sur cet endpoint. C'est la cause racine de votre reproduction — DELETE avec scope: FROM_THIS fait déjà le travail, le client se trompe d'appel.

Si vous voulez ce récapitulatif en document partageable plutôt qu'en scrollback, je peux le publier.


---

# Dernière discussion

Trois lots

Lot 1 — les garde-fous. Aucune décision requise, que du correctif. C'est ce qui saigne aujourd'hui :

- interdire is_recurring = true sans recurrence_rule — validation applicative + contrainte CHECK + migration qui nettoie les lignes existantes, dont celle de votre repro ;
- filtrer sur la règle dans TaskNotificationCandidateService et encadrer chaque série d'un try/catch dans le scheduler, pour qu'une ligne aberrante ne coupe plus toutes les notifications ;
- borner la boucle « moved in » de findOccurrencesForDateRange sur rrule_ends_at, pour que les MODIFIED orphelins cessent de s'afficher.

Lot 2 — la règle non rétroactive. Le cœur de ce qu'on vient de décider :

- toute modification de règle passe par troncature + successeur ;
- coupure repoussée après le dernier DONE ;
- suppression des MODIFIED/SKIPPED au-delà, avec le compte renvoyé au client pour l'alerte ;
- aligner updateSeriesFrom et replaceFollowing, qui ne s'accordent pas sur la date de départ du successeur ;
- migration ponctuelle nettoyant les orphelins déjà en base.

Ce lot change le comportement de l'API : il demande le travail client en parallèle.

Lot 3 — le confort. series_root_id, le retitrage du dialogue, l'alerte côté front.

Ce que je propose

Je démarre le lot 1 maintenant. Il ne dépend d'aucun arbitrage restant, il est entièrement couvrable par des tests, et il ferme une corruption de données et une panne de notifications qui sont actives en production.

Il reste une seule décision ouverte : series_root_id, oui ou non. Elle ne bloque pas le lot 1, mais c'est la seule qu'on ne pourra pas rattraper — une fois des chaînes de séries créées sans le lien, la relation est perdue. Si vous dites oui, je l'ajoute dans le lot 1 tant qu'on touche aux migrations.


# Dernière action effectuée

Lot 1 — les garde-fous

L'invariant is_recurring ⇒ recurrence_rule est posé aux quatre chemins d'écriture — création, patch, remplacement complet, et le successeur construit par updateSeriesFrom qui ne passe pas par replaceMutableFields. Doublé d'un CHECK en base (V26), avec la migration qui bascule les lignes existantes en non récurrentes — dont celle de votre reproduction.

Le test qui a cassé au passage était le bon signal : update_convertingTaskToRecurring convertissait une tâche en série sans fournir de règle, c'est-à-dire fabriquait exactement la ligne qu'on élimine. C'est la fixture que j'ai corrigée, pas la règle.

La panne de notifications est traitée à deux niveaux : eligibleRecurringTask filtre désormais sur la règle non nulle, et l'expansion de chaque série est isolée. Une règle syntaxiquement invalide — cas que le CHECK ne couvre pas — fait désormais perdre ses notifications à cette série seule, plus à toutes les autres.

La boucle « moved in » est bornée par la portée réelle de la série, donc les ancres hors de cette portée cessent de s'afficher par accident.

Lot 2 — les ancres détachées

Le mécanisme est en place de bout en bout : colonne detached (V27), marquage aux trois points de troncature, lecture par une requête indexée sur la date effective, et exposition dans les deux contrats.

Trois choses méritent d'être signalées explicitement :

La requête de lecture n'est pas adossée à la liste des séries actives. C'était le point délicat : une ancre détachée survit à la portée de sa série, donc findActiveRecurringTasksForPeriod ne la retrouverait jamais. Elle est collectée par sa propre date, avant la boucle principale.

Les SKIPPED sont supprimés, pas détachés — un saut est une absence, sans objet une fois l'occurrence disparue. La migration V27 applique la même règle aux orphelins déjà en base : SKIPPED supprimés, DONE et MODIFIED marqués détachés. Votre occurrence terminée puis orpheline par la scission devrait donc réapparaître au déploiement.

updateSeriesFrom honore enfin scheduledAt. Les deux chemins FROM_THIS divergeaient : le successeur était forcé à démarrer à l'instant de coupure. Votre scénario « à partir d'ici, passe au jeudi » est maintenant exprimable par les deux.

Ce qui reste

Le contrat OpenAPI porte isDetached en champ requis — les deux clients doivent être mis à jour. Android en particulier : la data class Gson devra accepter le champ, et surtout l'UI doit décider comment rendre une occurrence détachée. C'est le lot 3 avec le retitrage du dialogue et « Arrêter la série ».

Et deux points laissés ouverts en connaissance de cause : la fragmentation d'id assumée (on verra pour copier commentaires et sous-tâches), et le point de coupure fixé par défaut à la première occurrence après maintenant — une expression à un endroit si vous voulez autre chose.

