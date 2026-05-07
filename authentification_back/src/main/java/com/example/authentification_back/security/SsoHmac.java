package com.example.authentification_back.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * Utilitaires de signature HMAC utilisés par le flux SSO.
 */
public final class SsoHmac {

	private static final String HMAC_SHA256 = "HmacSHA256";

	private SsoHmac() {
	}

	/**
	 * Construit la charge utile canonique à signer.
	 *
	 * @param normalizedEmail email normalisé (trim + lower-case)
	 * @param nonce nonce anti-rejeu
	 * @param timestampEpochSeconds horodatage epoch secondes
	 * @return message canonique au format email:nonce:timestamp
	 */
	public static String messageToSign(String normalizedEmail, String nonce, long timestampEpochSeconds) {
		return normalizedEmail + ":" + nonce + ":" + timestampEpochSeconds;
	}

	/**
	 * Calcule une signature HMAC-SHA256 encodée en hexadécimal.
	 *
	 * @param password secret partagé utilisé comme clé HMAC
	 * @param message message canonique à signer
	 * @return signature hexadécimale
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
	 * Compare deux signatures hexadécimales en temps constant.
	 *
	 * @param aHex première signature
	 * @param bHex deuxième signature
	 * @return true si les signatures sont strictement égales
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
