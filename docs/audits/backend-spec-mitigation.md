Voici le tableau, regroupé par cause racine plutôt que par occurrence individuelle. C'est ce qui te permettra de corriger rapidement la majorité des 211 violations. Les exemples proviennent directement du rapport JUnit.

| Catégorie | Symptôme observé | Exemple | Fix recommandé |
|-----------|------------------|----------|----------------|
| Nullabilité absente dans la spec | L'API renvoie `null` alors que le schéma n'autorise que `string`, `integer` ou `boolean`. | `ProjectDto.parentId`, `TaskDto.*`, `CommentDto.taskId` | Déclarer explicitement les champs nullable (`type: [string, "null"]`, `type: [integer, "null"]`, etc.) dans les schémas OpenAPI. |
| ProblemDetails incomplet | Le backend ne renvoie pas tous les champs obligatoires du schéma `ProblemDetails`. | Les réponses 400/404 ne contiennent pas `type`. | Soit ajouter `type` dans toutes les réponses d'erreur du backend, soit retirer `type` de `required` dans la spec. |
| Contraintes métier absentes de la spec | La spec autorise des valeurs rejetées par le backend. | `estimateMinutes = 0` accepté par la spec mais refusé par Bean Validation. | Ajouter les contraintes (`minimum`, `maximum`, `exclusiveMinimum`, `minLength`, `pattern`, etc.) dans OpenAPI. |
| Validation métier incomplète | Valeurs techniquement valides mais métier invalides. | `AvailabilityRule.endMinute = 2147483646` → "Invalid availability rule". | Décrire précisément les bornes métier dans le contrat (`minimum: 0`, `maximum: 1439`, etc.). |
| additionalProperties implicite | Schemathesis ajoute des propriétés inconnues que Spring refuse. | Plusieurs POST retournent une erreur de validation. | Ajouter `additionalProperties: false` sur tous les objets qui ne doivent pas accepter de champs supplémentaires. |
| Réponses 500 inattendues | L'API renvoie une erreur serveur sur des requêtes conformes à la spec. | `/labels`, `/comments`, `/projects` | Corriger le backend : une requête valide ne doit jamais provoquer un 500. |
| Gestion incorrecte des ressources inexistantes | Certaines requêtes stateful produisent un comportement inattendu. | `/tasks/None` | Retourner systématiquement un 404 ou 400 documenté, jamais un 500. Vérifier également les liens stateful générés. |
| Réponses non conformes au contrat | Le JSON retourné ne respecte pas le schéma annoncé. | Plusieurs DTO contiennent des champs `null` non documentés. | Aligner les DTO exposés et les schémas OpenAPI. |
| Contrat trop permissif | Schemathesis génère des cas extrêmes que le backend refuse. | Entiers très grands, chaînes inhabituelles... | Rendre les schémas plus restrictifs (bornes numériques, tailles, formats, enums...). |
| Contrat backend/spec désynchronisé | Le comportement réel diverge de la documentation. | Plusieurs endpoints de création et de mise à jour. | Choisir une source de vérité (spec-first) puis aligner systématiquement backend et OpenAPI. |

Je les traiterais dans cet ordre de priorité :

| Priorité | Action | Impact attendu |
|----------|--------|----------------|
| 1 | Corriger la nullabilité des DTO | Élimine une très grosse partie des violations. |
| 2 | Aligner `ProblemDetails` | Supprime presque toutes les erreurs sur les réponses 4xx. |
| 3 | Ajouter les contraintes (`minimum`, `maximum`, `pattern`, etc.) | Réduit fortement les faux positifs générés par Schemathesis. |
| 4 | Ajouter `additionalProperties: false` | Évite les requêtes artificiellement invalides. |
| 5 | Corriger les véritables 500 backend | Ne laisse subsister que des erreurs fonctionnelles réelles. |
| 6 | Relancer Schemathesis et itérer | Le nombre d'échecs devrait alors devenir nettement plus faible et plus pertinent. |
