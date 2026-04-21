# Guide — Lancer SkillHub avec Docker

Ce guide décrit les commandes pour démarrer toute la stack (**MySQL**, **MongoDB**, **authentification Spring**, **Laravel**, **frontend Nginx**), puis exécuter les **migrations** et **seeders** Laravel.

**Schéma MySQL** : ni le **build Docker** ni le service **auth** (Spring) ne créent ou modifient les tables. C’est **`php artisan migrate`** dans le conteneur **`api`** qui applique le schéma (y compris la table **`utilisateurs`** partagée avec Spring). Après la première montée, lance toujours `migrate` (puis éventuellement `db:seed`) une fois MySQL prêt.

---

## 1. Prérequis

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) installé et **démarré** (Windows / macOS).
- Terminal ouvert à la **racine du dépôt** (`skillhub/`, là où se trouve `docker-compose.yml`).

---

## 2. Première montée (build + démarrage)

```bash
cd chemin/vers/skillhub
docker compose up --build
```

- La **première** exécution peut prendre plusieurs minutes (téléchargement des images de base, compilation Maven, `npm run build`, etc.).
- Laisse ce terminal ouvert : tu vois les logs de tous les services.

**Arrêter** : `Ctrl+C`, puis éventuellement :

```bash
docker compose down
```

Pour supprimer aussi les volumes MySQL/Mongo (données effacées) :

```bash
docker compose down -v
```

---

## 3. Lancer en arrière-plan (optionnel)

```bash
docker compose up --build -d
```

Voir les logs :

```bash
docker compose logs -f
```

Ou un seul service :

```bash
docker compose logs -f api
```

---

## 4. Ports une fois la stack démarrée

| Service    | URL / port hôte | Rôle |
|-----------|-----------------|------|
| Frontend  | http://localhost:5173 | Interface React (Nginx) |
| API Laravel | http://localhost:8000 | API métier `/api/...` |
| Auth Spring | http://localhost:8080 | API auth `/api/auth/...` |
| MySQL     | `localhost:13306` (utilisateur `skillhub` / mot de passe `skillhubpass`, base `skillhub`) | Accès depuis un client SQL sur l’hôte |
| MongoDB   | `localhost:27017` | Logs d’activité Laravel |

---

## 5. Migrations Laravel (obligatoire après le premier `up`)

Quand les conteneurs tournent (`api` et `mysql` en bonne santé), exécute **dans un second terminal** :

```bash
cd chemin/vers/skillhub
docker compose exec api php artisan migrate --force
```

- `--force` : nécessaire car `APP_ENV` peut être `production` dans certains contextes ; en local Docker c’est sans danger.
- Cette commande crée ou met à jour les tables Laravel dans la base **`skillhub`** du conteneur MySQL.

**Vérifier l’état des migrations** :

```bash
docker compose exec api php artisan migrate:status
```

---

## 6. Seeders Laravel (données de démo)

Le projet contient notamment **`CategorieFormationSeeder`** (catégories de formation), appelé depuis **`DatabaseSeeder`**.

```bash
docker compose exec api php artisan db:seed --force
```

Pour n’exécuter qu’un seeder précis :

```bash
docker compose exec api php artisan db:seed --class=CategorieFormationSeeder --force
```

**Raccourci** (migrate puis seed en une fois) :

```bash
docker compose exec api php artisan migrate --seed --force
```

> **Attention** : `php artisan migrate:fresh --seed` **supprime toutes les tables** puis recrée tout. À utiliser seulement si tu acceptes de perdre les données du volume MySQL Docker.

---

## 7. Ordre recommandé (résumé copier-coller)

À la racine du repo :

```bash
# 1. Démarrer la stack (première fois avec build)
docker compose up --build -d

# 2. Attendre ~30–60 s que MySQL soit prêt (healthcheck), puis :
docker compose exec api php artisan migrate --force

# 3. Données de démo (catégories, etc.)
docker compose exec api php artisan db:seed --force

# 4. (Optionnel) Redémarrer auth pour que le compte démo Spring (toto@example.com) soit créé
#    si les migrations n’étaient pas encore passées au premier démarrage de auth :
# docker compose restart auth
```

Ensuite ouvre **http://localhost:5173** dans le navigateur.

---

## 8. `APP_MASTER_KEY` (service Spring `auth`)

Le service **`auth`** a besoin d’une clé au démarrage. Par défaut, `docker-compose.yml` en définit une pour le **développement**.

Pour la personnaliser, crée un fichier **`.env`** à la **racine du repo** (à côté de `docker-compose.yml`) :

```env
APP_MASTER_KEY=ta_phrase_secrete_longue_minimum_recommandee
```

Puis :

```bash
docker compose up -d
```

---

## 9. Problèmes fréquents

| Symptôme | Piste |
|----------|--------|
| `api` ne joint pas MySQL | Vérifier que `mysql` est `healthy` : `docker compose ps` |
| `Table 'utilisateurs' already exists` au `migrate` | Ancienne base où JPA avait créé la table : `docker compose down -v` puis `up`, ou supprimer la table / la base à la main, puis **`migrate`** uniquement (Spring ne recrée plus les tables). |
| `formations` / `categorie_formations` introuvable | Migrations incomplètes : lancer `migrate --force` puis `db:seed --force`. Sinon repartir sur un volume propre : `docker compose down -v`, `up`, `migrate`, `seed`. |
| Erreur au `migrate` | `docker compose logs mysql` puis relancer `migrate` |
| Port déjà utilisé (8000, 8080, 5173) | Arrêter l’autre programme ou modifier les ports dans `docker-compose.yml` |
| Front sans données | Vérifier que `migrate` + `db:seed` ont bien été exécutés dans **`api`** |

---

## 10. PowerShell (Windows)

Même commandes ; le répertoire typique :

```powershell
cd D:\tp\skillhub
docker compose up --build -d
docker compose exec api php artisan migrate --force
docker compose exec api php artisan db:seed --force
```

Si `docker compose` n’est pas reconnu, essaye `docker-compose` (ancienne syntaxe).

---

Pour l’architecture globale (auth Spring + Laravel + front), voir aussi **[`ARCHITECTURE-SKILLHUB.md`](./ARCHITECTURE-SKILLHUB.md)**.
