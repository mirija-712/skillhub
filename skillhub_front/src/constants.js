/**
 * Base URL des API « métier » Laravel (formations, modules, inscriptions aux cours, …).
 * En dev, la valeur par défaut "/api" est proxifiée par Vite vers le serveur Laravel (ex. :8000).
 */
export const API_URL = import.meta.env.VITE_API_URL || "/api";

/**
 * Base URL du service d'authentification Spring (inscription, connexion, profil).
 * En dev, "/auth-api" est proxifié par Vite vers Spring en réécrivant vers son préfixe "/api".
 * En production, définir VITE_AUTH_API_URL vers l'URL HTTPS réelle du service auth.
 */
export const AUTH_API_URL = import.meta.env.VITE_AUTH_API_URL || "/auth-api";

/** Base Laravel pour /storage (ex. http://localhost:8000). Optionnel si le proxy Vite suffit. */
export const API_ORIGIN = String(import.meta.env.VITE_API_ORIGIN || "").replace(/\/$/, "");

export const IMG_PLACEHOLDER = "https://via.placeholder.com/300x180?text=Formation";
