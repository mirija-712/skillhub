# SkillHub — Backend (Laravel)

Backend API REST du projet SkillHub. **L’authentification compte (inscription / connexion / jeton) est déléguée au service Spring `authentification_back`** : Laravel n’expose plus de routes `/api/auth/*`. Les routes métier protégées utilisent le middleware **`auth.remote`**, qui vérifie le `Bearer` auprès de Spring puis injecte l’utilisateur dans la requête.

Rôles **apprenant** (`participant`) et **formateur**, catégories, formations (CRUD, upload d’images), **inscriptions aux formations** (suivi de cours — *pas* la création de compte), et **historisation** (ActivityLogService, logs structurés).

Architecture des trois services : **[`../README.md`](../README.md)**.

## Architecture du service Laravel

```text
skillhub_back/
├─ app/
│  ├─ Http/
│  │  ├─ Controllers/Api/       # Endpoints metier (formations, modules, inscriptions)
│  │  ├─ Middleware/            # auth.remote, formateur, apprenant
│  │  └─ Requests/              # Validation des donnees entrantes
│  ├─ Models/                   # Entites Eloquent
│  ├─ Services/                 # Services metier (ex: ActivityLogService)
│  └─ Providers/
├─ routes/
│  └─ api.php                   # Definition des routes REST
├─ database/
│  ├─ migrations/               # Schema MySQL
│  ├─ factories/
│  └─ seeders/
├─ storage/                     # Fichiers (images formations, logs)
├─ config/
├─ tests/
├─ artisan
└─ README.md
```

- Ce service porte la **logique metier** (formations, modules, inscriptions, progression).
- La validation du token est deleguee a Spring via le middleware `auth.remote`.
- Les regles d'acces par role sont appliquees ici (`formateur`, `apprenant`).

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

### Notation des formations (apprenant)

Cette fonctionnalité permet à un apprenant **authentifié et inscrit** à une formation de laisser un avis.

- **POST** `/api/formations/{id}/noter` — Créer une note + commentaire pour une formation.
  - Auth: `Authorization: Bearer <token>` (token validé par `auth.remote`).
  - Accès: rôle `participant` uniquement.
  - Corps JSON attendu:

```json
{
  "note": 4,
  "commentaire": "Très bonne formation, j'ai beaucoup appris !"
}
```

Règles métier:
- Un apprenant ne peut noter une formation **qu'une seule fois**.
- La `note` doit être un entier **entre 1 et 5**.
- L'apprenant doit être **inscrit** à la formation.

Codes de réponse pour `POST /api/formations/{id}/noter`:
- **201** — Rating créé avec succès (`rating` renvoyé en JSON).
- **400** — Note invalide ou formation déjà notée par cet apprenant.
- **403** — Apprenant non inscrit à la formation (ou rôle non apprenant).
- **401** — Requête sans token ou token invalide.

### Détail formation enrichi

L'endpoint public **GET** `/api/formations/{id}` inclut désormais:
- `note_moyenne` — moyenne des notes (arrondie à 2 décimales, `null` s'il n'y a aucun avis),
- `nombre_avis` — nombre total d'avis.

#### Exemple de réponse `GET /api/formations/{id}` (avec métriques d'avis)

```json
{
  "formation": {
    "id": 12,
    "nom": "Laravel Avancé",
    "description": "API et architecture",
    "note_moyenne": 4.33,
    "nombre_avis": 3
  }
}
```

### Détails techniques de l'implémentation (notation)

Cette section décrit précisément les fichiers modifiés et le comportement métier.

#### 1) Route API

Fichier: `routes/api.php`

```php
Route::middleware('auth.remote')->group(function () {
    Route::middleware('apprenant')->group(function () {
        Route::post('formations/{id}/noter', [RatingController::class, 'store'])
            ->whereNumber('id');
    });
});
```

Effets:
- `auth.remote` vérifie le `Bearer` auprès de Spring (`/auth/me`).
- `apprenant` exige le rôle `participant`.

#### 2) Modèle `Rating`

Fichier: `app/Models/Rating.php`

```php
class Rating extends Model
{
    protected $table = 'ratings';

    protected $fillable = [
        'user_id',
        'formation_id',
        'note',
        'commentaire',
    ];

    public function user(): BelongsTo
    {
        return $this->belongsTo(Utilisateur::class, 'user_id');
    }

    public function formation(): BelongsTo
    {
        return $this->belongsTo(Formation::class, 'formation_id');
    }
}
```

#### 3) Migration `ratings`

Fichier: `database/migrations/2026_05_08_100000_create_ratings_table.php`

```php
Schema::create('ratings', function (Blueprint $table) {
    $table->id();
    $table->foreignId('user_id')->constrained('utilisateurs')->cascadeOnDelete();
    $table->foreignId('formation_id')->constrained('formations')->cascadeOnDelete();
    $table->unsignedTinyInteger('note');
    $table->text('commentaire');
    $table->timestamps();

    $table->unique(['user_id', 'formation_id']);
});
```

Points importants:
- FK vers `utilisateurs` et `formations`.
- contrainte d'unicité `(user_id, formation_id)` pour garantir un seul avis.
- la plage de note (1..5) est validée côté contrôleur (retour `400`).

#### 4) Contrôleur de notation

Fichier: `app/Http/Controllers/Api/RatingController.php`

```php
public function store(Request $request, int $id): JsonResponse
{
    $user = $request->user();
    if (! $user) {
        return response()->json(['message' => 'Token manquant ou invalide. Veuillez vous reconnecter.'], 401);
    }

    $formation = Formation::find($id);
    if (! $formation) {
        return response()->json(['message' => 'Formation introuvable'], 404);
    }

    $isInscrit = Inscription::where('utilisateur_id', (int) $user->id)
        ->where('formation_id', $formation->id)
        ->exists();
    if (! $isInscrit) {
        return response()->json(['message' => 'Vous devez être inscrit à cette formation pour la noter.'], 403);
    }

    $validator = Validator::make($request->all(), [
        'note' => ['required', 'integer', 'between:1,5'],
        'commentaire' => ['required', 'string'],
    ]);
    if ($validator->fails()) {
        return response()->json(['message' => 'Note invalide.', 'erreurs' => $validator->errors()], 400);
    }

    $alreadyRated = Rating::where('user_id', (int) $user->id)
        ->where('formation_id', $formation->id)
        ->exists();
    if ($alreadyRated) {
        return response()->json(['message' => 'Vous avez déjà noté cette formation.'], 400);
    }

    $rating = Rating::create([
        'user_id' => (int) $user->id,
        'formation_id' => $formation->id,
        'note' => (int) $request->input('note'),
        'commentaire' => (string) $request->input('commentaire'),
    ]);

    return response()->json(['rating' => $rating], 201);
}
```

Ordre logique des contrôles:
1. Authentification (`401`)
2. Existence formation (`404`)
3. Inscription de l'apprenant (`403`)
4. Validation payload (`400`)
5. Doublon de notation (`400`)
6. Création (`201`)

#### 5) Enrichissement du détail formation (moyenne + nombre d'avis)

Fichier: `app/Http/Controllers/Api/FormationController.php`

```php
$formation = Formation::with(['formateur:id,nom,prenom', 'categorie:id,libelle', 'modules'])
    ->withCount('inscriptions')
    ->withCount('ratings')
    ->withAvg('ratings', 'note')
    ->find($id);

$formation->note_moyenne = $formation->ratings_avg_note !== null
    ? round((float) $formation->ratings_avg_note, 2)
    : null;
$formation->nombre_avis = (int) $formation->ratings_count;
unset($formation->ratings_avg_note, $formation->ratings_count);
```

#### 6) Relations Eloquent ajoutées

Fichiers:
- `app/Models/Formation.php`
- `app/Models/Utilisateur.php`

```php
// Formation.php
public function ratings(): HasMany
{
    return $this->hasMany(Rating::class, 'formation_id');
}

// Utilisateur.php
public function ratings(): HasMany
{
    return $this->hasMany(Rating::class, 'user_id');
}
```

### Scénarios de test manuel (Postman / curl)

> Remplacer `{{BASE_URL}}` (ex: `http://localhost:8000`) et `{{TOKEN_APPRENANT}}`.

#### 1) Cas nominal (201)

```bash
curl -X POST "{{BASE_URL}}/api/formations/1/noter" \
  -H "Authorization: Bearer {{TOKEN_APPRENANT}}" \
  -H "Content-Type: application/json" \
  -d "{\"note\":4,\"commentaire\":\"Très bonne formation, j'ai beaucoup appris !\"}"
```

#### 2) Même apprenant note 2 fois (400)

Refaire exactement la même requête.

#### 3) Note invalide (400)

```bash
curl -X POST "{{BASE_URL}}/api/formations/1/noter" \
  -H "Authorization: Bearer {{TOKEN_APPRENANT}}" \
  -H "Content-Type: application/json" \
  -d "{\"note\":6,\"commentaire\":\"Invalide\"}"
```

#### 4) Apprenant non inscrit (403)

Utiliser un token d'apprenant qui n'a pas d'inscription à la formation ciblée.

#### 5) Sans token (401)

```bash
curl -X POST "{{BASE_URL}}/api/formations/1/noter" \
  -H "Content-Type: application/json" \
  -d "{\"note\":4,\"commentaire\":\"Avis\"}"
```

### Scénarios de test automatisé (PHPUnit)

Fichier de test: `tests/Feature/RatingControllerTest.php`

Commandes:

```bash
php artisan test --filter=RatingControllerTest
php artisan test --filter=FormationControllerTest
```

Extrait des assertions clés:

```php
$response->assertStatus(201)->assertJsonPath('rating.note', 4);
$response->assertStatus(400); // déjà noté
$response->assertStatus(400); // note hors plage
$response->assertStatus(403); // non inscrit
$response->assertStatus(401); // sans token
```

### Dépannage rapide (erreurs fréquentes)

#### `php artisan migrate` échoue avec `SQLSTATE[HY000] [2002]`

Cause probable: MySQL non démarré ou mauvais host/port dans `.env`.

Checklist:
- vérifier `DB_HOST`, `DB_PORT`, `DB_DATABASE`, `DB_USERNAME`, `DB_PASSWORD`
- démarrer le service DB (local ou Docker)
- vider le cache config:

```bash
php artisan config:clear
php artisan migrate
```

#### Les tests passent mais `migrate` échoue

C'est possible: les tests utilisent souvent SQLite en mémoire (`phpunit.xml`) alors que `migrate` utilise ta DB de `.env`.

#### Regle metier : maximum 5 formations par apprenant

Lors d’un `POST /api/formations/{formationId}/inscription`, l’API doit refuser l’inscription si l’apprenant a deja 5 inscriptions actives.

Exemple de controle dans `InscriptionController::store` :

```php
$inscriptionsCount = Inscription::where('utilisateur_id', $userId)->count();
if ($inscriptionsCount >= 5) {
    return response()->json([
        'message' => 'Vous ne pouvez pas vous inscrire à plus de 5 formations.',
    ], 422);
}
```

Ce controle vient **apres** la verification "deja inscrit", et **avant** `Inscription::create(...)`.

Images : `storage/app/public/formations/`. Historisation : `App\Services\ActivityLogService` (consultation, inscription, création, modification, suppression de formations).

---

## 6. Modèles et base de données

- **utilisateurs** : `id`, `email`, `mot_de_passe`, `nom`, `prenom` (nullable), `role` (participant, formateur), `created_at`, `updated_at` ; colonnes supplémentaires possibles gérées par Spring (ex. jeton de session, lockout) selon migrations JPA.
- **categorie_formations** : `id`, `libelle`, `created_at`, `updated_at`.
- **formations** : id, id_formateur, id_categorie, nom, description, duree_heures, prix, level, statut, image_url, created_at, updated_at.
- **inscriptions** : id, utilisateur_id, formation_id, progression, date_inscription, created_at.
- **ratings** : `id`, `user_id`, `formation_id`, `note` (1..5), `commentaire`, `created_at`, `updated_at`.  
  Contrainte d'unicité: (`user_id`, `formation_id`) pour garantir un seul avis par apprenant et par formation.

---

## 7. Documentation Swagger

Après `php artisan l5-swagger:generate`, la documentation est disponible à : **http://localhost:8000/api/documentation**.

---

## 8. Tests

Tests avec **SQLite en mémoire** (`phpunit.xml`).

- `php artisan test` — Tous les tests.
- `php artisan test --filter FormationControllerTest` — Tests formations uniquement.
- `php artisan test --filter RatingControllerTest` — Tests de notation (201/400/403/401).

**FormationControllerTest** : 401 sans token ; 201 avec formateur valide ; apprenant ne peut pas créer de formation (403) ; formateur ne peut pas modifier la formation d’un autre (403). Les tests **AuthTest** historiques (routes `/api/auth/*` Laravel) peuvent être obsolètes si les routes auth locales ont été retirées.

**RatingControllerTest** couvre:
- apprenant inscrit + note valide => **201**
- même apprenant qui note 2 fois => **400**
- note hors plage 1..5 => **400**
- apprenant non inscrit => **403**
- requête sans token => **401**

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
- **403** — Ou apprenant non inscrit qui tente de noter une formation.
- **404** — Ressource introuvable.
- **422** — Erreur de validation ; corps : `message` et `erreurs`.
- **400** — Données métier invalides pour la notation (note invalide, déjà noté).
- **500** — Erreur serveur.
