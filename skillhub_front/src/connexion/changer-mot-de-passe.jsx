import { useState } from "react";
import { Link } from "react-router-dom";
import NavbarPublic from "../components/NavbarPublic";
import Footer from "../components/Footer";
import { authApi } from "../api/auth";
import { getMessageErreurApi } from "../api/utils";
import { evaluatePassword } from "./passwordPolicy";
import "./css/login.css";
import "./css/inscription.css";

export default function ChangerMotDePasse() {
  const [formData, setFormData] = useState({
    oldPassword: "",
    newPassword: "",
    confirmPassword: "",
  });
  const [erreur, setErreur] = useState("");
  const [succes, setSucces] = useState("");
  const [chargement, setChargement] = useState(false);
  const passwordState = evaluatePassword(formData.newPassword);
  const confirmationOk =
    formData.confirmPassword.length > 0 &&
    formData.newPassword === formData.confirmPassword;

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    setErreur("");
    setSucces("");
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErreur("");
    setSucces("");
    if (!passwordState.isCompliant) {
      setErreur("Le nouveau mot de passe ne respecte pas la politique de sécurité.");
      return;
    }
    if (!confirmationOk) {
      setErreur("La confirmation du nouveau mot de passe ne correspond pas.");
      return;
    }
    setChargement(true);
    try {
      const res = await authApi.changePassword(formData);
      setSucces(res.message || "Mot de passe changé avec succès.");
      setFormData({ oldPassword: "", newPassword: "", confirmPassword: "" });
    } catch (err) {
      setErreur(getMessageErreurApi(err, "Impossible de changer le mot de passe."));
    } finally {
      setChargement(false);
    }
  };

  return (
    <>
      <NavbarPublic />
      <main className="page-auth">
        <div className="conteneur-auth">
          <div className="carte-auth">
            <h1 className="titre-auth">Changer mon mot de passe</h1>
            <p className="sous-titre-auth">Mettez à jour votre mot de passe en toute sécurité.</p>

            <form onSubmit={handleSubmit} className="formulaire-auth">
              <div className="champ-auth">
                <label htmlFor="old-password" className="libelle-auth">
                  Mot de passe actuel
                </label>
                <input
                  id="old-password"
                  name="oldPassword"
                  type="password"
                  className="champ-saisie-auth"
                  value={formData.oldPassword}
                  onChange={handleChange}
                  required
                  autoComplete="current-password"
                />
              </div>

              <div className="champ-auth">
                <label htmlFor="new-password" className="libelle-auth">
                  Nouveau mot de passe
                </label>
                <input
                  id="new-password"
                  name="newPassword"
                  type="password"
                  className="champ-saisie-auth"
                  value={formData.newPassword}
                  onChange={handleChange}
                  required
                  minLength={12}
                  autoComplete="new-password"
                />
              </div>

              <div className="password-strength-wrap" aria-live="polite">
                <div className={`password-strength-bar level-${passwordState.level}`}>
                  <div className="password-strength-fill" style={{ width: `${(passwordState.score / 5) * 100}%` }} />
                </div>
                <p className={`password-strength-label level-${passwordState.level}`}>
                  Force:{" "}
                  {passwordState.level === "strong"
                    ? "fort"
                    : passwordState.level === "medium"
                      ? "moyen"
                      : passwordState.level === "weak"
                        ? "faible"
                        : "en attente"}
                </p>
                <ul className="password-rules">
                  {passwordState.checks.map((rule) => (
                    <li key={rule.key} className={rule.ok ? "ok" : "ko"}>
                      {rule.ok ? "✓" : "•"} {rule.label}
                    </li>
                  ))}
                </ul>
              </div>

              <div className="champ-auth">
                <label htmlFor="confirm-password" className="libelle-auth">
                  Confirmer le nouveau mot de passe
                </label>
                <input
                  id="confirm-password"
                  name="confirmPassword"
                  type="password"
                  className="champ-saisie-auth"
                  value={formData.confirmPassword}
                  onChange={handleChange}
                  required
                  minLength={12}
                  autoComplete="new-password"
                />
                {formData.confirmPassword.length > 0 && !confirmationOk && (
                  <p className="erreur-auth">La confirmation doit correspondre au nouveau mot de passe.</p>
                )}
              </div>

              {succes && <p className="succes-auth">{succes}</p>}
              {erreur && <p className="erreur-auth">{erreur}</p>}

              <button
                type="submit"
                className="bouton-auth bouton-auth-principal"
                disabled={chargement || !passwordState.isCompliant || !confirmationOk}
              >
                {chargement ? "Mise à jour..." : "Mettre à jour"}
              </button>

              <p className="lien-bas-auth">
                <Link to="/" className="lien-auth">
                  Retour à l&apos;accueil
                </Link>
              </p>
            </form>
          </div>
        </div>
      </main>
      <Footer />
    </>
  );
}
