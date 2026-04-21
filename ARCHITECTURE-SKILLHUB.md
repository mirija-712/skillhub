# Architecture SkillHub — trois services

Ce document décrit **comment les trois parties travaillent ensemble** en local ou en déploiement. Le détail fichier par fichier reste dans le code (commentaires d’en-tête) et dans chaque `README.md`.

---

## Vue d’ensemble

| Service | Rôle | Port typique (dev) |
|--------|------|---------------------|
| **skillhub_front** | Interface React (Vite) : pages publiques, connexion, dashboards, appels API | `5173` |
| **authentification_back** | API Spring : **compte utilisateur** (inscription, connexion, profil, jeton de session) sur la table **`utilisateurs`** | `8080` |
| **skillhub_back** | API Laravel : **métier** (formations, modules, **inscription à une formation**, logs…) ; **ne gère plus** l’auth locale | `8000` |

**Deux sens du mot « inscription »**

- **Inscription au site** (créer un compte) → `authentification_back` (`POST …/auth/inscription`).
- **Inscription à une formation** (apprenant s’inscrit à un cours) → `skillhub_back` (`POST …/formations/{id}/inscription`).

---

## Flux d’authentification (séquence)

1. L’utilisateur remplit le formulaire sur **skillhub_front**.
2. Le front appelle **`AUTH_API_URL`** (en dev via Vite : préfixe `/auth-api` → proxy vers Spring `…/api`).
3. **authentification_back** valide les données, écrit/ lit la table **`utilisateurs`**, renvoie un **jeton opaque** (UUID stocké en base) + profil (`id`, `email`, `nom`, `prenom`, `role`).
4. Le front garde `token` et `utilisateur` dans **`localStorage`**.
5. Pour les routes protégées Laravel (formations CRUD, progression, etc.), le front envoie **`Authorization: Bearer <token>`** vers **`/api/...`** (Laravel).
6. Le middleware **`auth.remote`** dans Laravel appelle **`GET {AUTHENTIFICATION_API_URL}/auth/me`** avec le même Bearer. Spring répond le profil si le jeton est valide.
7. Laravel injecte un **objet utilisateur minimal** (`id`, `email`, `nom`, `prenom`, `role`) dans la requête ; les contrôleurs utilisent `$request->user()` comme avant pour les droits (formateur / apprenant).

**Important** : le token vient de **Spring**, pas d’un JWT Laravel. Les routes `/api/auth/*` ont été retirées de Laravel.

---

## Variables d’environnement utiles

### skillhub_front

| Variable | Rôle |
|----------|------|
| `VITE_API_URL` | Base des appels métier (défaut `/api` → proxy Vite vers Laravel) |
| `VITE_AUTH_API_URL` | Base des appels auth (défaut `/auth-api` → proxy Vite vers Spring `/api`) |
| `VITE_API_ORIGIN` | Optionnel : origine absolue pour `/storage` (images) |

### skillhub_back (`.env`)

| Variable | Rôle |
|----------|------|
| `AUTHENTIFICATION_API_URL` | URL de base de l’API Spring **avec** `/api` final, ex. `http://127.0.0.1:8080/api` |
| `AUTHENTIFICATION_API_TIMEOUT` | Timeout HTTP vers Spring (secondes) |
| `DB_*` | Même base MySQL que Spring pour la table **`utilisateurs`** (données cohérentes) |

### authentification_back

| Variable / propriété | Rôle |
|---------------------|------|
| `AUTH_DB_URL`, `AUTH_DB_USERNAME`, `AUTH_DB_PASSWORD` | Connexion MySQL (surcharge de `application.properties`) |
| `APP_MASTER_KEY` | Encore requis au démarrage tant que le bean `PasswordEncryptionService` est présent (chiffrement legacy TP) ; définir une valeur locale factice |

---

## Fichiers « carte » par service

### authentification_back

- `AuthentificationBackApplication.java` — démarrage Spring Boot.
- `controller/AuthController.java` — endpoints REST (`/api/auth/inscription`, `/auth/connexion`, `/auth/me`, etc.).
- `service/AuthService.java` — règles métier : hash mot de passe (BCrypt), lockout, jeton session.
- `entity/User.java` — mapping JPA table **`utilisateurs`**.
- `resources/application.properties` — datasource, port `8080`.

### skillhub_back

- `routes/api.php` — routes publiques vs groupe **`auth.remote`**.
- `app/Http/Middleware/AuthenticateWithAuthService.php` — validation du Bearer via Spring.
- `bootstrap/app.php` — alias middleware `auth.remote`.
- `config/services.php` — `authentification.base_url` / timeout.
- `app/Http/Controllers/Api/*` — formations, modules, **InscriptionController** (inscription **aux cours**, pas au site).

### skillhub_front

- `src/constants.js` — `API_URL` vs `AUTH_API_URL`.
- `src/api/auth.js` — inscription / connexion / me / déconnexion (côté Spring).
- `src/api/formations.js` (et autres) — métier Laravel avec en-tête Bearer.
- `vite.config.js` — proxys `/api` → Laravel, `/auth-api` → Spring.

---

## Démarrage local (rappel)

1. MySQL avec la base configurée (ex. `skillhub_perso`).
2. `authentification_back` : `.\mvnw.cmd spring-boot:run` (+ `APP_MASTER_KEY` si besoin).
3. `skillhub_back` : `php artisan serve` (ou vhost Wamp).
4. `skillhub_front` : `npm run dev`.

En cas d’erreur Windows « fichier de pagination » / mémoire : libérer de la RAM, augmenter le fichier d’échange, ou servir Laravel via Apache au lieu de `artisan serve`.

---

## Docker Compose (stack complète)

Fichier racine : **`docker-compose.yml`**.

| Service | Image / build | Port hôte | Rôle |
|--------|-----------------|-----------|------|
| `mysql` | `mysql:8.0` | `13306` → 3306 | Base `skillhub` (utilisateurs partagés) |
| `mongo` | `mongo:7.0` | `27017` | Logs d’activité Laravel |
| **`auth`** | `./authentification_back` | `8080` | API Spring d’authentification |
| `api` | `./skillhub_back` | `8000` | Laravel ; `AUTHENTIFICATION_API_URL=http://auth:8080/api` |
| `frontend` | `./skillhub_front` | `5173` → 80 | Nginx : `/api/` → Laravel, **`/auth-api/`** → Spring (`/api/` côté Spring) |

Démarrage : à la racine du repo, `docker compose up --build`.  
Surcharge optionnelle de **`APP_MASTER_KEY`** pour `auth` : fichier `.env` à la racine ou `export APP_MASTER_KEY=...` avant `docker compose up`.

**Guide pas à pas** (migrations, seeders, commandes) : **[`GUIDE-DOCKER.md`](./GUIDE-DOCKER.md)**.

---

## CI/CD (GitHub Actions)

Fichier **`.github/workflows/ci.yml`** :

1. **`authentification`** — Java 17, cache Maven, `./mvnw verify` (tests + JaCoCo), `APP_MASTER_KEY` factice pour Surefire.
2. **`backend`** — PHP / Composer / PHPUnit (inchangé).
3. **`frontend`** — Node / lint / tests / build (inchangé).
4. **`docker-images`** — dépend des trois jobs ; `docker compose config`, pull MySQL/Mongo, build des images **`skillhub-auth`**, **`skillhub-api`**, **`skillhub-front`** (tags `:${{ github.sha }}`).
