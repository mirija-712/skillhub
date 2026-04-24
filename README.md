# SkillHub — Monorepo complet

## Architecture du projet (dossiers et sous-dossiers)

```text
skillhub/
├─ .github/
│  └─ workflows/                      # CI/CD
├─ authentification_back/             # Service Auth (Spring Boot)
│  ├─ src/
│  │  ├─ main/
│  │  │  ├─ java/com/example/authentification_back/
│  │  │  │  ├─ controller/            # Endpoints auth
│  │  │  │  ├─ service/               # Logique métier auth
│  │  │  │  ├─ repository/            # Accès données
│  │  │  │  ├─ model/                 # Entités
│  │  │  │  ├─ dto/                   # Objets requête/réponse
│  │  │  │  └─ config/                # Config sécurité/app
│  │  │  └─ resources/                # application.properties
│  │  └─ test/                        # Tests Spring
│  ├─ Dockerfile
│  └─ README.md
├─ skillhub_back/                     # Service API métier (Laravel)
│  ├─ app/
│  │  ├─ Http/
│  │  │  ├─ Controllers/Api/          # Endpoints formations/modules/inscriptions
│  │  │  ├─ Middleware/               # auth.remote, rôles
│  │  │  └─ Requests/                 # Validation des inputs
│  │  ├─ Models/                      # Modèles Eloquent
│  │  └─ Services/                    # Services métier (ex: logs)
│  ├─ routes/
│  │  └─ api.php
│  ├─ database/
│  │  ├─ migrations/
│  │  └─ seeders/
│  ├─ storage/
│  ├─ tests/
│  └─ README.md
├─ skillhub_front/                    # Frontend (React + Vite)
│  ├─ src/
│  │  ├─ api/                         # Clients API (auth, formations, inscriptions)
│  │  ├─ components/                  # Composants UI réutilisables
│  │  ├─ connexion/                   # Login / inscription / compte
│  │  ├─ pages/
│  │  │  ├─ public/
│  │  │  ├─ espace-client/
│  │  │  └─ espace-formateur/
│  │  ├─ constants.js                 # URLs API
│  │  ├─ App.jsx
│  │  └─ main.jsx
│  ├─ public/
│  ├─ vite.config.js                  # Proxys /api et /auth-api
│  └─ README.md
├─ docker-compose.yml                 # Orchestration des services + DB
└─ README.md
```

### Lecture rapide

- `authentification_back` : gère comptes, token et identité utilisateur.
- `skillhub_back` : gère tout le métier e-learning (formations, modules, inscriptions).
- `skillhub_front` : interface utilisateur qui consomme les 2 APIs.

---

Plateforme e-learning avec 3 services :

- `skillhub_front` : frontend React/Vite.
- `authentification_back` : API Spring Boot dédiée aux comptes (inscription, connexion, profil, changement de mot de passe).
- `skillhub_back` : API Laravel métier (formations, modules, inscriptions aux formations, progression, journalisation).

L’objectif de l’architecture est de **séparer l’authentification** (Spring) de la logique métier (Laravel), tout en offrant une expérience unifiée côté frontend.

---

## 1) Architecture globale

### Services

1. **Frontend (React + Vite)**
   - Port dev : `5173`
   - Appelle :
     - `AUTH_API_URL` (par défaut `/auth-api`) pour auth Spring.
     - `API_URL` (par défaut `/api`) pour endpoints métier Laravel.

2. **Auth Service (Spring Boot)**
   - Port : `8080`
   - Gère les comptes et sessions (token opaque UUID).
   - Endpoints principaux : `POST /api/auth/inscription`, `POST /api/auth/connexion`, `GET /api/auth/me`, `PUT /api/auth/change-password`.

3. **API Métier (Laravel)**
   - Port : `8000`
   - Gère catégories, formations, modules, inscriptions apprenants, progression.
   - Protège les routes via middleware `auth.remote` qui valide le Bearer token auprès de Spring (`GET /auth/me`).

### Bases de données

- **MySQL** (`skillhub`) : données relationnelles partagées (utilisateurs, formations, inscriptions, etc.).
- **MongoDB** (`skillhub_activity_log`) : logs d’activité métier (consultations, créations, inscriptions, etc.).

---

## 2) Communication entre les 3 services

## Vue réseau en développement

- Navigateur -> Front (`http://localhost:5173`)
- Front -> Spring Auth via proxy Vite : `/auth-api/*` -> `http://localhost:8080/api/*`
- Front -> Laravel API via proxy Vite : `/api/*` -> `http://localhost:8000/api/*`

## Flux 1 : inscription / connexion

1. L’utilisateur remplit le formulaire sur le frontend.
2. Le frontend envoie la requête à Spring (`/auth/inscription` ou `/auth/connexion`).
3. Spring valide les données, lit/écrit dans MySQL, renvoie un token.
4. Le frontend stocke ce token (localStorage) et l’utilise ensuite en `Authorization: Bearer`.

## Flux 2 : appel métier protégé (ex: s’inscrire à une formation)

1. Le frontend appelle Laravel (`POST /api/formations/{id}/inscription`) avec le Bearer.
2. Le middleware Laravel `auth.remote` appelle Spring (`GET /api/auth/me`) avec ce même Bearer.
3. Si Spring valide, Laravel injecte l’utilisateur dans la requête (`$request->user()`), vérifie rôle/règles métier, puis traite l’action.
4. Laravel persiste en MySQL et peut tracer l’action en MongoDB via `ActivityLogService`.

## Flux 3 : rôle et contrôle d’accès

- Le frontend utilise `authApi.me()` pour protéger les routes UI.
- Laravel applique ensuite une sécurité serveur avec middlewares `formateur` / `apprenant`.
- Donc même si l’UI est contournée, l’API bloque l’accès non autorisé côté serveur.

---

## 3) Fonctionnalités couvertes

- Authentification :
  - Inscription formateur
  - Connexion/déconnexion
  - Profil courant (`/auth/me`)
  - Changement de mot de passe
- Catalogue :
  - Liste/détail des formations (public)
  - Catégories
- Espace formateur :
  - CRUD formations
  - Gestion modules de formation
- Espace apprenant :
  - Inscription/désinscription à une formation
  - Suivi de progression (formation + modules)

### Règle métier ajoutée

- Un apprenant ne peut pas être inscrit à plus de **5 formations** simultanément.

### Nouvelle fonctionnalité : limite de 5 inscriptions par étudiant

Cette fonctionnalité empêche un étudiant (rôle apprenant/participant) de dépasser 5 formations actives.

#### Objectif

- Eviter qu’un même étudiant monopolise trop de formations.
- Garantir une charge d’apprentissage réaliste.
- Encadrer la progression pédagogique côté plateforme.

#### Comportement attendu côté API

- Endpoint concerné : `POST /api/formations/{formationId}/inscription`.
- Si l’étudiant a moins de 5 inscriptions : inscription créée (HTTP `201`).
- Si l’étudiant a déjà 5 inscriptions : inscription refusée (HTTP `422` ou `403` selon implémentation) avec un message explicite.

#### Message fonctionnel recommandé

```text
Vous ne pouvez pas vous inscrire à plus de 5 formations.
```

#### Exemple de code backend (Laravel)

Dans `skillhub_back/app/Http/Controllers/Api/InscriptionController.php`, méthode `store` :

```php
public function store(Request $request, int $formationId): JsonResponse
{
    $userId = (int) $request->user()->id;

    $formation = Formation::find($formationId);
    if (! $formation) {
        return response()->json(['message' => 'Formation introuvable'], 404);
    }

    $exists = Inscription::where('utilisateur_id', $userId)
        ->where('formation_id', $formationId)
        ->exists();
    if ($exists) {
        return response()->json(['message' => 'Vous êtes déjà inscrit à cette formation.'], 422);
    }

    $inscriptionsCount = Inscription::where('utilisateur_id', $userId)->count();
    if ($inscriptionsCount >= 5) {
        return response()->json([
            'message' => 'Vous ne pouvez pas vous inscrire à plus de 5 formations.',
        ], 422);
    }

    Inscription::create([
        'utilisateur_id' => $userId,
        'formation_id' => $formationId,
        'progression' => 0,
    ]);

    app(ActivityLogService::class)->logCourseEnrollment($userId, $formationId);

    return response()->json(['message' => 'Inscription enregistrée.'], 201);
}
```

#### Explication du code

- `count()` calcule le nombre d’inscriptions déjà actives pour l’utilisateur.
- Le contrôle `if ($inscriptionsCount >= 5)` bloque toute nouvelle inscription au-delà de la limite.
- Le retour `422` indique une règle métier invalide (la requête est comprise mais non autorisée dans cet état).
- La vérification “déjà inscrit” reste utile, car elle évite les doublons même avant la limite des 5.
- En cas de succès, l’inscription est créée puis journalisée via `ActivityLogService`.

#### Exemple de gestion côté frontend (React)

Dans l’écran d’inscription à une formation, afficher le message API :

```js
try {
  await inscriptionsApi.inscrire(formationId);
  setSuccess("Inscription enregistrée.");
} catch (err) {
  setError(err?.message || "Impossible de vous inscrire.");
}
```

Avec cette gestion, si l’API renvoie la limite des 5 formations, le message s’affiche directement dans l’interface.

#### Impact frontend

- Afficher l’erreur renvoyée par l’API dans l’UI (toast/alerte/message formulaire).
- Ne pas considérer le clic comme réussi si la limite est atteinte.
- Conserver l’état courant de la liste des formations sans incohérence.

#### Cas de test conseillés

- Etudiant avec 4 inscriptions -> nouvelle inscription acceptée.
- Etudiant avec 5 inscriptions -> nouvelle inscription refusée.
- Etudiant qui se désinscrit (retour à 4) -> nouvelle inscription redevient possible.
- Tentative de double inscription à la même formation -> toujours bloquée indépendamment de la limite.

---

## 4) Lancer le projet avec Docker (recommandé)

Depuis la racine du monorepo :

```bash
docker compose up --build
```

Services exposés :

- Front : `http://localhost:5173`
- Laravel : `http://localhost:8000`
- Spring Auth : `http://localhost:8080`
- MySQL : `localhost:13306`
- MongoDB : `localhost:27017`

Arrêter :

```bash
docker compose down
```

---

## 5) Lancer en local (sans Docker)

## 5.1 Auth Spring

```bash
cd authentification_back
mvn spring-boot:run
```

Configurer `application.properties` et la connexion MySQL.

## 5.2 API Laravel

```bash
cd skillhub_back
composer install
cp .env.example .env
php artisan key:generate
php artisan migrate
php artisan serve
```

Variables importantes côté Laravel :

- `AUTHENTIFICATION_API_URL` (ex: `http://localhost:8080/api`)
- `AUTHENTIFICATION_API_TIMEOUT`
- `MONGODB_URI`, `MONGODB_DB`, `MONGODB_COLLECTION`

## 5.3 Frontend React

```bash
cd skillhub_front
npm install
npm run dev
```

Variables possibles côté front :

- `VITE_API_URL` (par défaut `/api`)
- `VITE_AUTH_API_URL` (par défaut `/auth-api`)
- `VITE_API_ORIGIN` (optionnel, pour ressources `/storage`)

---

## 6) Arborescence du monorepo

```text
skillhub/
├─ authentification_back/   # Spring Boot (auth)
├─ skillhub_back/           # Laravel (métier)
├─ skillhub_front/          # React/Vite (UI)
├─ docker-compose.yml
└─ GUIDE-DOCKER.md
```

---

## 7) Endpoints clés

### Auth (Spring)

- `POST /api/auth/inscription`
- `POST /api/auth/connexion`
- `GET /api/auth/me`
- `PUT /api/auth/change-password`

### Métier (Laravel)

- Public :
  - `GET /api/categories`
  - `GET /api/formations`
  - `GET /api/formations/{id}`
- Formateur :
  - `POST /api/formations`
  - `PUT /api/formations/{id}`
  - `DELETE /api/formations/{id}`
  - `POST /api/formations/{formationId}/modules`
- Apprenant :
  - `POST /api/formations/{formationId}/inscription`
  - `DELETE /api/formations/{formationId}/inscription`
  - `GET /api/apprenant/formations`
  - `PUT /api/formations/{formationId}/progression`

---

## 8) Tests et qualité

### Front

```bash
cd skillhub_front
npm run lint
```

### Laravel

```bash
cd skillhub_back
php artisan test
```

### Spring

```bash
cd authentification_back
mvn test
```

---

## 9) Documentation existante par service

- `skillhub_front/README.md`
- `skillhub_back/README.md`
- `authentification_back/README.md`
- `GUIDE-DOCKER.md`

Ce `README.md` racine sert de guide d’ensemble pour comprendre et exécuter tout le projet.
