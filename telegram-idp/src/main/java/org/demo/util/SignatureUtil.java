package org.demo.util;


import jakarta.ws.rs.core.MultivaluedMap;
import lombok.extern.jbosslog.JBossLog;
import org.demo.AuthenticationException;
import org.demo.Constraint;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.SortedMap;
import java.util.TreeMap;

@JBossLog
public class SignatureUtil {
    private static SignatureUtil instance;
    private SignatureUtil() {
    }
    public static SignatureUtil getInstance() {
        if (instance == null) {
            synchronized (SignatureUtil.class) {
                if (instance == null) {
                    instance = new SignatureUtil();
                }
            }
        }
        return instance;
    }
    public static String generateSecureSignature(String message,String key) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(keySpec);
        byte[] hmacBytes = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
        String base64 = Base64.getUrlEncoder().withoutPadding().encodeToString(hmacBytes);
        return base64.substring(0, 25);
    }

    public String validateSignature(String sessionId, String receivedSignature, String token) throws AuthenticationException {
        if (sessionId == null || !sessionId.contains("_")) {
            throw new AuthenticationException("Missing or invalid 'start' parameter.", "invalid_request", "The login link is incomplete or malformed. Please try again.");
        }
        log.debugf("Signature: %s; Session Data: %s; Token: %s", receivedSignature, sessionId, token);

        try {
            String expectedSignature = SignatureUtil.generateSecureSignature(sessionId, token);
            log.debugf("Expected Signature: %s", expectedSignature);
            if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8), receivedSignature.getBytes(StandardCharsets.UTF_8))) {
                throw new AuthenticationException("Invalid session signature.", "invalid_credentials", "Invalid login link. Please try logging in again.");
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }

        return sessionId;
    }

    public void validateTelegramDataHash(MultivaluedMap<String, String> queryParams, String botToken) throws AuthenticationException {
        String providedHash = queryParams.getFirst(Constraint.TELEGRAM_HASH_PARAM);
        if (providedHash == null) {
            throw new AuthenticationException("Missing 'hash' parameter.", "invalid_request", "Could not verify login data. Please try again.");
        }

        String authDateStr = queryParams.getFirst("auth_date");
        if (authDateStr == null) {
            throw new AuthenticationException("Missing 'auth_date' parameter.", "invalid_request", "The login data is incomplete. Please try again.");
        }

        try {
            long authDate = Long.parseLong(authDateStr);
            if (Instant.now().getEpochSecond() - authDate > 300) { // 5 minute validity
                throw new AuthenticationException("Telegram data has expired.", "expired_code", "This login link has expired. Please generate a new one.");
            }
        } catch (NumberFormatException e) {
            throw new AuthenticationException("Invalid 'auth_date' format.", "invalid_request", "The login data is malformed. Please try again.");
        }

        SortedMap<String, String> dataMap = new TreeMap<>();
        for (String key : queryParams.keySet()) {
            if (!Constraint.TELEGRAM_HASH_PARAM.equals(key) && !Constraint.TELEGRAM_START_PARAM.equals(key)) {
                String value = queryParams.getFirst(key);
                if (value != null) {
                    dataMap.put(key, value);
                }
            }
        }
    }
}
