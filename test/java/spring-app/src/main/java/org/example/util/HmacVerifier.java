package org.example.util;

import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Locale;

/** Validates "X-Hub-Signature-256: sha256=<hex>" using constant-time comparison. */
@Component
public class HmacVerifier {
	 /**
     * Verify the "X-Hub-Signature-256" header against the HMAC of the body using the provided secret.
     *
     * @param secret    WEBHOOK_SECRET 
     * @param body      raw request body bytes
     * @param headerSig header value, expected format: "sha256=<64-hex>"
     * @return true if valid; false otherwise
     */
    public boolean verifySha256(String secret, byte[] body, String headerSig) {
        if (secret == null || secret.isBlank() || headerSig == null || headerSig.isBlank()) return false;

        String sig = headerSig.trim();
        int idx = sig.indexOf('=');
        if (idx < 0) return false;

        String algo = sig.substring(0, idx).toLowerCase(Locale.ROOT);
        if (!"sha256".equals(algo)) return false;

        String hexDigest = sig.substring(idx + 1).trim().toLowerCase(Locale.ROOT);
        byte[] provided = hexToBytes(hexDigest);
        byte[] expected = hmacSha256(secret.getBytes(), body);

        return MessageDigest.isEqual(expected, provided);
    }

    /**
     * Compute HMAC-SHA256(key, message).
     *
     * @param key     secret key bytes 
     * @param message raw request body bytes
     * @return 32-byte HMAC digest
     */
    private static byte[] hmacSha256(byte[] key, byte[] message) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(message);
        } catch (Exception e) {
            throw new IllegalStateException("HMAC computation failed", e);
        }
    }

    /**
     * Decode a lowercase/uppercase hex string into bytes.
     *
     * @param s  even-length hex string 
     * @return   decoded bytes
     * @throws   IllegalArgumentException if the string contains non-hex chars or has odd length
    */
    private static byte[] hexToBytes(String s) {
        int len = s.length();
        if ((len & 1) != 0) throw new IllegalArgumentException("Odd hex length");
        byte[] out = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            int hi = Character.digit(s.charAt(i), 16);
            int lo = Character.digit(s.charAt(i + 1), 16);
            if (hi < 0 || lo < 0) throw new IllegalArgumentException("Invalid hex chars");
            out[i / 2] = (byte) ((hi << 4) + lo);
        }
        return out;
    }
}
