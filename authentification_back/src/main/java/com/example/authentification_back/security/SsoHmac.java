package com.example.authentification_back.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * Utilitaires statiques de signature HMAC pour un flux type SSO (canonisation du message, signature hex, comparaison constante).
 * <p>
 * <b>Rôle</b> : centraliser la construction du message signé et le calcul HMAC-SHA256 sans dépendance Spring,
 * afin de limiter les erreurs de concaténation et les comparaisons vulnérables aux timings attacks.
 *
 * @author SkillHub
 * @version 0.0.1-SNAPSHOT
 */
public final class SsoHmac {

	private static final String HMAC_SHA256 = "HmacSHA256";

	private SsoHmac() {
	}

	/**
	 * Assemble la chaîne canonique {@code email:nonce:timestamp} à passer au MAC (ordre et séparateurs fixes).
	 *
	 * @param normalizedEmail adresse déjà normalisée (ex. minuscules) pour éviter les ambiguïtés de signature
	 * @param nonce valeur unique côté protocole pour limiter la réutilisation de requêtes interceptées
	 * @param timestampEpochSeconds instant de la demande en secondes depuis epoch UTC (tolérance gérée ailleurs)
	 * @return texte UTF-8 à signer tel quel par {@link #hmacSha256Hex(String, String)}
	 */
	public static String messageToSign(String normalizedEmail, String nonce, long timestampEpochSeconds) {
		return normalizedEmail + ":" + nonce + ":" + timestampEpochSeconds;
	}

	/**
	 * Calcule le tag HMAC-SHA256 du message avec la clé dérivée du secret utilisateur (mot de passe ou secret applicatif).
	 *
	 * @param password matière secrète servant de clé symétrique pour {@link Mac#getInstance(String)}
	 * @param message chaîne produite par {@link #messageToSign(String, String, long)} ou équivalent strict
	 * @return représentation hexadécimale minuscule du tag (pour comparaison avec {@link #constantTimeEqualsHex(String, String)})
	 * @throws IllegalStateException si l’algorithme {@code HmacSHA256} n’est pas fourni par le JRE
	 */
	public static String hmacSha256Hex(String password, String message) {
		try {
			Mac mac = Mac.getInstance(HMAC_SHA256);
			mac.init(new SecretKeySpec(password.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
			byte[] tag = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(tag);
		} catch (Exception e) {
			throw new IllegalStateException("HMAC-SHA256 indisponible", e);
		}
	}

	/**
	 * Compare deux tags hexadécimaux sans court-circuit sur les octets (via {@link MessageDigest#isEqual(byte[], byte[])}) après parsing.
	 *
	 * @param aHex première empreinte hex (ex. attendue côté serveur)
	 * @param bHex deuxième empreinte hex (ex. reçue du client)
	 * @return {@code true} si les deux séquences d’octets décodées sont identiques ; {@code false} si null, longueurs différentes ou parse invalide
	 */
	public static boolean constantTimeEqualsHex(String aHex, String bHex) {
		if (aHex == null || bHex == null || aHex.length() != bHex.length()) {
			return false;
		}
		try {
			byte[] a = HexFormat.of().parseHex(aHex);
			byte[] b = HexFormat.of().parseHex(bHex);
			return MessageDigest.isEqual(a, b);
		} catch (IllegalArgumentException e) {
			return false;
		}
	}
}
