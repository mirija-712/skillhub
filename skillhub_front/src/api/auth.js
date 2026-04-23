/**
 * Module client « compte utilisateur » : parle au service Spring {@code authentification_back}
 * via la constante `AUTH_API_URL` (voir `constants.js`), pas à Laravel.
 *
 * <ul>
 *   <li><b>inscription / connexion</b> : JSON aligné avec les DTO Spring ({@code mot_de_passe}, {@code role}, …).</li>
 *   <li><b>Jeton</b> : UUID opaque renvoyé par Spring (pas un JWT Laravel) ; envoyé ensuite en Bearer vers Laravel pour le métier.</li>
 *   <li><b>me</b> : vérifie le jeton et recharge le profil ; normalise la réponse (objet à plat ou clé utilisateur) pour l'UI.</li>
 *   <li><b>deconnexion</b> : côté Spring l’endpoint peut être absent ; on nettoie toujours le localStorage.</li>
 * </ul>
 */
import { parseJsonResponse } from "./utils";
import { AUTH_API_URL } from "../constants";

export const authApi = {
  /** Inscription compte : Spring attend email, mot_de_passe, nom, prenom (optionnel), role. */
  async inscription(data) {
    const res = await fetch(`${AUTH_API_URL}/auth/inscription`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data),
    });
    const json = await parseJsonResponse(res);
    if (!res.ok) throw { status: res.status, ...json };
    return {
      ...json,
      message: json.message || "Utilisateur créé avec succès. Vous pouvez maintenant vous connecter.",
    };
  },

  /** Connexion : email + mot_de_passe → Spring renvoie token (UUID) + profil (champs à plat ou encapsulés). */
  async connexion(data) {
    const res = await fetch(`${AUTH_API_URL}/auth/connexion`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data),
    });
    const json = await parseJsonResponse(res);
    if (!res.ok) throw { status: res.status, ...json };
    if (json.utilisateur && json.token) return json;
    return {
      token: json.token,
      utilisateur: {
        id: json.id,
        email: json.email,
        nom: json.nom,
        prenom: json.prenom,
        role: json.role,
      },
    };
  },

  // Vérifie si le token est encore valide et récupère les infos de l'utilisateur connecté.
  // Utilisé par ProtectedRoute pour valider le token et le rôle.
  async me() {
    const token = localStorage.getItem("token");
    if (!token) return null;
    const res = await fetch(`${AUTH_API_URL}/auth/me`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    const json = await parseJsonResponse(res);
    if (!res.ok) return null;
    if (json.utilisateur) return json.utilisateur;
    return {
      id: json.id,
      email: json.email,
      nom: json.nom,
      prenom: json.prenom,
      role: json.role,
    };
  },

  async deconnexion() {
    const token = localStorage.getItem("token");
    if (!token) return { message: "Déconnexion réussie" };
    const res = await fetch(`${AUTH_API_URL}/auth/deconnexion`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
    });
    const json = await parseJsonResponse(res);
    if (!res.ok) return { message: "Déconnexion locale effectuée." };
    return json;
  },

  async changePassword(data) {
    const token = localStorage.getItem("token");
    const res = await fetch(`${AUTH_API_URL}/auth/change-password`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(data),
    });
    const json = await parseJsonResponse(res);
    if (!res.ok) throw { status: res.status, ...json };
    return json;
  },

  // Getters/setters pour le token et l'utilisateur. On les met en localStorage pour qu'ils survivent au rechargement de page.
  getToken() {
    return localStorage.getItem("token");
  },

  setToken(token) {
    localStorage.setItem("token", token);
  },

  removeToken() {
    localStorage.removeItem("token");
    localStorage.removeItem("utilisateur");
  },

  setUtilisateur(utilisateur) {
    if (utilisateur) {
      localStorage.setItem("utilisateur", JSON.stringify(utilisateur));
    } else {
      localStorage.removeItem("utilisateur");
    }
  },

  getUtilisateur() {
    const u = localStorage.getItem("utilisateur");
    return u ? JSON.parse(u) : null;
  },
};
