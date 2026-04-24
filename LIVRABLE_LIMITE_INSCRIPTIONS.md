# Livrable — Limite de 5 inscriptions simultanées par apprenant

## Objectif

Empêcher un apprenant de s'inscrire à plus de **5 formations simultanément**.

Le comportement attendu est :
- blocage de la 6e inscription ;
- retour backend avec **HTTP 400** ;
- affichage d'un message d'erreur côté front.

## Portée du livrable

### Backend

Fichier modifié :
- `skillhub_back/app/Http/Controllers/Api/InscriptionController.php`

Changements :
- ajout d'une constante `MAX_SIMULTANEOUS_ENROLLMENTS = 5` ;
- dans `store()`, ajout d'un contrôle du nombre d'inscriptions actives pour l'apprenant ;
- si la limite est atteinte : retour JSON avec code **400**.

Réponse renvoyée quand la limite est atteinte :

```json
{
  "message": "Vous avez atteint la limite de 5 formations suivies simultanément."
}
```

### Frontend

Fichier modifié :
- `skillhub_front/src/pages/public/FormationDetail.jsx`

Changements :
- ajout de l'état `erreurInscription` ;
- affichage du message d'erreur backend en cas d'échec d'inscription (hors cas `422` déjà inscrit) ;
- l'utilisateur voit donc clairement la raison du refus lorsque la limite de 5 est atteinte.

## Correctif complémentaire (disponibilité auth)

Fichier modifié :
- `skillhub_back/app/Http/Middleware/AuthenticateWithAuthService.php`

Contexte :
- des erreurs `Service d'authentification indisponible` bloquaient certaines actions protégées (ex: ajout formation).

Correctif :
- stratégie de fallback sur plusieurs URL candidates du service d'auth selon le contexte d'exécution (Docker/local) ;
- logs conservés pour faciliter le diagnostic ;
- maintien du retour `503` si aucun endpoint auth n'est joignable.

## Scénarios de test effectués

1. **Inscription <= 5 formations**
   - action : inscription d'un apprenant à une nouvelle formation ;
   - attendu : succès (`201`) ;
   - résultat : OK.

2. **Inscription à la 6e formation**
   - action : tentative d'inscription alors que l'apprenant suit déjà 5 formations ;
   - attendu : refus (`400`) + message métier ;
   - résultat : OK.

3. **Affichage erreur côté front**
   - action : tenter la 6e inscription depuis la page détail ;
   - attendu : message d'erreur visible dans l'UI ;
   - résultat : OK.

4. **Routes protégées quand auth indisponible**
   - action : simuler indisponibilité du service auth ;
   - attendu : retour `503` explicite ;
   - résultat : OK.

## Critères d'acceptation

- [x] Un apprenant ne peut pas dépasser 5 inscriptions simultanées.
- [x] Le backend renvoie `400` quand la limite est atteinte.
- [x] Le front affiche l'erreur de façon lisible.
- [x] Le flux existant "déjà inscrit" (`422`) reste inchangé.

## Risques / points d'attention

- Le contrôle est réalisé au moment de l'inscription ; si des inscriptions concurrentes massives sont possibles, une contrainte DB ou un verrou applicatif pourrait être envisagé en renfort.
- Le correctif auth suppose des URLs standards (`auth`, `localhost`, `host.docker.internal`) ; vérifier l'environnement cible si différent.

## Plan de validation post-merge

- vérifier en environnement d'intégration :
  - inscription 1 à 5 : OK ;
  - 6e inscription : `400` + message ;
  - affichage message sur l'écran détail formation ;
  - ajout de formation fonctionne avec auth disponible.

## Message PR suggéré

**Titre**
`feat(apprenant): limiter les inscriptions simultanées à 5 formations`

**Description courte**
Ce livrable introduit une règle métier empêchant un apprenant de suivre plus de 5 formations simultanément. Le backend renvoie désormais un `400` explicite à la 6e tentative, et le front affiche l'erreur utilisateur. Un correctif de robustesse a également été appliqué sur le middleware d'authentification distante.
