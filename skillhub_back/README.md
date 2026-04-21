# SkillHub — Backend (Laravel)

Backend API REST du projet SkillHub. **L’authentification compte (inscription / connexion / jeton) est déléguée au service Spring `authentification_back`** : Laravel n’expose plus de routes `/api/auth/*`. Les routes métier protégées utilisent le middleware **`auth.remote`**, qui vérifie le `Bearer` auprès de Spring puis injecte l’utilisateur dans la requête.

Rôles **apprenant** (`participant`) et **formateur**, catégories, formations (CRUD, upload d’images), **inscriptions aux formations** (suivi de cours — *pas* la création de compte), et **historisation** (ActivityLogService, logs structurés).

Architecture des trois services : **[`../ARCHITECTURE-SKILLHUB.md`](../ARCHITECTURE-SKILLHUB.md)**.

---

## 1. Prérequis

- **PHP 8.2** ou plus
- **Composer**
- **MySQL** ou **SQLite**
- Extensions PHP : `mbstring`, `openssl`, `pdo`, `tokenizer`, `xml`, `ctype`, `json`, `bcmath`

---

## 2. Structure du projet

```
skillhub_back/
├── app/
│   ├── Http/
│   │   ├── Controllers/
│   │   │   ├── Controller.php
│   │   │   └── Api/
│   │   │       ├── CategorieFormationController.php  # GET liste des catégories
│   │   │       └── FormationController.php  # CRUD formations (index paginé, show, store, update, destroy)
│   │   ├── Requests/
│   │   │   ├── RegisterRequest.php          # Validation inscription (prenom nullable)
│   │   │   ├── StoreFormationRequest.php   # title, description, price, duration, level, id_categorie?, image?
│   │   │   └── UpdateFormationRequest.php  # nom, description, level, duree_heures, prix, statut, image?
│   │   └── Middleware/
│   │       ├── AuthenticateWithAuthService.php  # Valide Bearer via authentification_back (alias auth.remote)
│   │       ├── VerifierFormateur.php       # Accès réservé aux formateurs
│   │       └── VerifierApprenant.php       # Accès réservé aux participants (apprenants)
│   ├── Models/
│   │   ├── CategorieFormation.php
│   │   ├── Formation.php                    # nom, description, duree_heures, prix, level, statut, image_url…
│   │   └── Utilisateur.php
│   └── Providers/
├── bootstrap/
│   └── app.php                             # Gestion des exceptions (401, 403, 422, 500)
├── config/
│   ├── auth.php                            # Guard JWT (api)
│   ├── l5-swagger.php                      # Documentation Swagger
│   └── …
├── database/
│   ├── migrations/
│   │   ├── 2026_03_04_170642_create_users.php
│   │   ├── 2026_03_04_170721_create_categorie_formation.php
│   │   ├── 2026_03_04_170743_create_formations.php
│   │   ├── 2026_03_04_180000_remove_date_heure_description_from_formations.php
│   │   ├── 2026_03_06_000001_add_title_description_level_to_formations.php  # description, level
│   │   └── 2026_03_06_000002_drop_title_from_formations.php
│   ├── factories/
│   └── seeders/
├── routes/
│   └── api.php                             # Routes API (préfixe /api) ; groupe auth.remote pour le métier authentifié
├── storage/
│   └── app/public/formations/              # Images uploadées (storage:link)
├── tests/
│   └── Feature/
│       └── FormationControllerTest.php     # 401 sans token, 201 avec formateur valide
├── .env
├── artisan
├── composer.json
├── openapi.yaml                            # Documentation OpenAPI (manuel)
└── phpunit.xml                             # Tests (SQLite en mémoire)
```

---

## 3. Commandes utiles

| Commande | Description |
|----------|-------------|
| `composer install` | Installe les dépendances PHP. |
| `cp .env.example .env` | Crée le fichier d’environnement (Windows : `copy .env.example .env`). |
| `php artisan key:generate` | Génère la clé de l’application. |
| `php artisan jwt:secret` | Génère la clé secrète JWT. |
| `php artisan migrate` | Crée ou met à jour les tables. |
| `php artisan migrate:fresh` | Recrée la base et relance les migrations. |
| `php artisan storage:link` | Lien `public/storage` → `storage/app/public` pour les images. |
| `php artisan serve` | Serveur de développement sur http://localhost:8000. |
| `php artisan test` | Lance les tests PHPUnit. |
| `php artisan test --filter FormationControllerTest` | Lance uniquement les tests FormationController. |
| `php artisan l5-swagger:generate` | Génère la doc Swagger (OpenAPI). |
| `php artisan route:list` | Liste des routes. |

---

## 4. Installation pas à pas

1. `cd skillhub_back`
2. `composer install`
3. `cp .env.example .env` (ou `copy .env.example .env` sous Windows)
4. `php artisan key:generate` puis `php artisan jwt:secret`
5. Configurer la base dans `.env` (SQLite ou MySQL)
6. `php artisan migrate`
7. `php artisan storage:link`
8. `php artisan serve` → API sur **http://localhost:8000**

---

## 5. API exposée

Toutes les routes sont préfixées par **`/api`**.

### Authentification des comptes

Gérée par **`authentification_back`** (Spring), pas par ce dépôt. Configurer **`AUTHENTIFICATION_API_URL`** dans `.env` (ex. `http://127.0.0.1:8080/api`).

Les routes métier ci-dessous exigent **`Authorization: Bearer <token>`** où le token est celui renvoyé par Spring à la connexion.

### Catégories (public)

- **GET** `/api/categories` — Liste des catégories. Pas d’authentification.

### Formations (liste et détail publics)

- **GET** `/api/formations` — Liste paginée. Query : `recherche`, `id_categorie`, `level`, `id_formateur`, `statut`, `page`, `per_page`. Sans token = toutes les formations ; avec token formateur et sans `id_formateur` = ses formations.
- **GET** `/api/formations/{id}` — Détail d’une formation.
### Formations (création / modification / suppression, formateur uniquement)

- **POST** `/api/formations` — Création (formateurs uniquement). `id_formateur` pris de l’utilisateur connecté.  
  Corps (JSON) : `title`, `description`, `price`, `duration`, `level` (obligatoires), `id_categorie` (optionnel).  
  Corps (multipart) : idem + `image` (optionnel, jpeg/png/jpg/gif/webp, max 2 Mo).  
  Mapping : title→nom, duration→duree_heures, price→prix. Réponse enrichie : `formation` avec title, description, price, duration, level.

- **PUT** `/api/formations/{id}` — Mise à jour (JSON). Champs : nom, description, level, duree_heures, prix, statut, id_categorie. Réponse enrichie comme en création.
- **POST** `/api/formations/{id}` — Mise à jour avec nouvelle image (FormData). Même règle d’autorisation (propriétaire uniquement).

- **DELETE** `/api/formations/{id}` — Suppression (et suppression de l’image sur disque si présente).

### Inscriptions (apprenant)

- **POST** `/api/formations/{formationId}/inscription` — S’inscrire à une formation.
- **DELETE** `/api/formations/{formationId}/inscription` — Se désinscrire.
- **GET** `/api/apprenant/formations` — Formations suivies (avec progression).
- **PUT** `/api/formations/{formationId}/progression` — Mettre à jour la progression (0–100).

Images : `storage/app/public/formations/`. Historisation : `App\Services\ActivityLogService` (consultation, inscription, création, modification, suppression de formations).

---

## 6. Modèles et base de données

- **utilisateurs** : `id`, `email`, `mot_de_passe`, `nom`, `prenom` (nullable), `role` (participant, formateur), `created_at`, `updated_at` ; colonnes supplémentaires possibles gérées par Spring (ex. jeton de session, lockout) selon migrations JPA.
- **categorie_formations** : `id`, `libelle`, `created_at`, `updated_at`.
- **formations** : id, id_formateur, id_categorie, nom, description, duree_heures, prix, level, statut, image_url, created_at, updated_at.
- **inscriptions** : id, utilisateur_id, formation_id, progression, date_inscription, created_at.

---

## 7. Documentation Swagger

Après `php artisan l5-swagger:generate`, la documentation est disponible à : **http://localhost:8000/api/documentation**.

---

## 8. Tests

Tests avec **SQLite en mémoire** (`phpunit.xml`).

- `php artisan test` — Tous les tests.
- `php artisan test --filter FormationControllerTest` — Tests formations uniquement.

**FormationControllerTest** : 401 sans token ; 201 avec formateur valide ; apprenant ne peut pas créer de formation (403) ; formateur ne peut pas modifier la formation d’un autre (403). Les tests **AuthTest** historiques (routes `/api/auth/*` Laravel) peuvent être obsolètes si les routes auth locales ont été retirées.

---

## 9. Variables d’environnement (.env)

- `APP_KEY` — Clé application (`key:generate`).
- `DB_CONNECTION` — `sqlite` ou `mysql`.
- `DB_DATABASE` — Fichier SQLite ou nom de base MySQL.
- `AUTHENTIFICATION_API_URL` — URL base de l’API Spring (**avec** suffixe `/api`).
- `AUTHENTIFICATION_API_TIMEOUT` — Timeout des appels HTTP vers Spring.
- Clé JWT (`php artisan jwt:secret`) — encore présente dans la config si le package JWT est installé ; le flux SkillHub actuel utilise le **token Spring** sur les routes `auth.remote`.

---

## 10. Codes HTTP

- **401** — Token manquant, invalide ou expiré.
- **403** — Accès formateurs uniquement, ou formation appartenant à un autre formateur.
- **404** — Ressource introuvable.
- **422** — Erreur de validation ; corps : `message` et `erreurs`.
- **500** — Erreur serveur.
