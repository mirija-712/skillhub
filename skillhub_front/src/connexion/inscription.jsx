// Inscription avec choix du rôle - style SkillHub1.0
import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import NavbarPublic from "../components/NavbarPublic";
import Footer from "../components/Footer";
import { authApi } from "../api/auth";
import { getMessageErreurApi } from "../api/utils";
import { evaluatePassword } from "./passwordPolicy";
import "./css/login.css";
import "./css/inscription.css";

function Inscription() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    email: "",
    mot_de_passe: "",
    confirm_mot_de_passe: "",
    nom: "",
    prenom: "",
    role: "participant",
  });
  const [erreur, setErreur] = useState("");
  const [succes, setSucces] = useState("");
  const [chargement, setChargement] = useState(false);
  const passwordState = evaluatePassword(formData.mot_de_passe);
  const confirmationOk =
    formData.confirm_mot_de_passe.length > 0 &&
    formData.mot_de_passe === formData.confirm_mot_de_passe;

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    setErreur("");
  };

  // Envoie les données au back ; en cas de succès, redirige vers la page de connexion après 2 s
  const handleSubmit = async (e) => {
    e.preventDefault();
    setErreur("");
    setSucces("");
    if (!passwordState.isCompliant) {
      setErreur("Le mot de passe ne respecte pas la politique de sécurité.");
      return;
    }
    if (!confirmationOk) {
      setErreur("La confirmation du mot de passe ne correspond pas.");
      return;
    }
    setChargement(true);
    try {
      const res = await authApi.inscription(formData);
      setSucces(
        res.message ||
          "Utilisateur créé avec succès. Vous pouvez maintenant vous connecter.",
      );
      setFormData({
        email: "",
        mot_de_passe: "",
        confirm_mot_de_passe: "",
        nom: "",
        prenom: "",
        role: formData.role,
      });
      setTimeout(() => {
        navigate("/");
      }, 2000);
    } catch (err) {
      setErreur(
        getMessageErreurApi(
          err,
          "Erreur d'inscription. Vérifiez que le backend est démarré.",
        ),
      );
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
          <h1 className="titre-auth">Inscription</h1>
          <p className="sous-titre-auth">Créez votre compte SkillHub</p>

          <form onSubmit={handleSubmit} className="formulaire-auth">
            <div className="ligne-auth">
              <div className="champ-auth">
                <label htmlFor="inscription-prenom" className="libelle-auth">
                  Prénom
                </label>
                <input
                  id="inscription-prenom"
                  name="prenom"
                  type="text"
                  className="champ-saisie-auth"
                  placeholder="prenom"
                  value={formData.prenom}
                  onChange={handleChange}
                />
              </div>
              <div className="champ-auth">
                <label htmlFor="inscription-nom" className="libelle-auth">
                  Nom
                </label>
                <input
                  id="inscription-nom"
                  name="nom"
                  type="text"
                  className="champ-saisie-auth"
                  placeholder="nom"
                  value={formData.nom}
                  onChange={handleChange}
                  required
                />
              </div>
            </div>

            <div className="champ-auth">
              <label htmlFor="inscription-email" className="libelle-auth">
                Email
              </label>
              <input
                id="inscription-email"
                name="email"
                type="email"
                className="champ-saisie-auth"
                placeholder="exemple@email.com"
                value={formData.email}
                onChange={handleChange}
                required
                autoComplete="email"
              />
            </div>

            <div className="champ-auth">
              <label htmlFor="inscription-role" className="libelle-auth">
                Je suis
              </label>
              <select
                id="inscription-role"
                name="role"
                className="champ-saisie-auth"
                value={formData.role}
                onChange={handleChange}
                required
              >
                <option value="participant">Apprenant</option>
                <option value="formateur">Formateur</option>
              </select>
            </div>

            <div className="champ-auth">
              <label
                htmlFor="inscription-mot-de-passe"
                className="libelle-auth"
              >
                Mot de passe
              </label>
              <input
                id="inscription-mot-de-passe"
                name="mot_de_passe"
                type="password"
                className="champ-saisie-auth"
                placeholder="••••••••"
                value={formData.mot_de_passe}
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
              <label htmlFor="inscription-confirm-mot-de-passe" className="libelle-auth">
                Confirmer le mot de passe
              </label>
              <input
                id="inscription-confirm-mot-de-passe"
                name="confirm_mot_de_passe"
                type="password"
                className="champ-saisie-auth"
                placeholder="••••••••"
                value={formData.confirm_mot_de_passe}
                onChange={handleChange}
                required
                minLength={12}
                autoComplete="new-password"
              />
              {formData.confirm_mot_de_passe.length > 0 && !confirmationOk && (
                <p className="erreur-auth">La confirmation doit correspondre au mot de passe.</p>
              )}
            </div>

            {succes && <p className="succes-auth">{succes}</p>}
            {erreur && <p className="erreur-auth">{erreur}</p>}
            <button
              type="submit"
              className="bouton-auth bouton-auth-principal"
              disabled={chargement || !passwordState.isCompliant || !confirmationOk}
            >
              {chargement ? "Inscription..." : "S'inscrire"}
            </button>

            <p className="lien-bas-auth">
              Déjà un compte ?{" "}
              <Link to="/connexion" className="lien-auth">
                Se connecter
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

export default Inscription;
