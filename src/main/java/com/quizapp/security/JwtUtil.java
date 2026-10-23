package com.quizapp.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizapp.entity.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;

@Component
public class JwtUtil {

    @Value("${jwt.secret:quiz_app_super_secret_key_for_jwt_authentication_256bit_key_long_enough}")
    private String secret;

    @Value("${jwt.expiration-ms:604800000}") // 7 days default
    private long expirationMs;

    @Value("${jwt.cookie-name:jwt_token}")
    private String cookieName;

    @Value("${jwt.payload-encryption-key:quiz_app_aes_256_gcm_payload_secret_key_32bytes!!}")
    private String payloadEncryptionKey;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SecureRandom secureRandom = new SecureRandom();

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            return Keys.hmacShaKeyFor(padded);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private byte[] getEncryptionKeyBytes() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(payloadEncryptionKey.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Encrypts plain text using AES-256-GCM.
     * Output format: Base64Url( 12-byte IV + ciphertext_with_16byte_auth_tag )
     */
    public String encryptPayload(String plainText) {
        try {
            byte[] iv = new byte[12];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(getEncryptionKeyBytes(), "AES");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);

            byte[] cipherTextWithTag = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            ByteBuffer buffer = ByteBuffer.allocate(iv.length + cipherTextWithTag.length);
            buffer.put(iv);
            buffer.put(cipherTextWithTag);

            return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer.array());
        } catch (Exception e) {
            throw new RuntimeException("AES-GCM encryption failed: " + e.getMessage(), e);
        }
    }

    /**
     * Decrypts Base64Url(IV + ciphertext + auth_tag) using AES-256-GCM.
     */
    public String decryptPayload(String cipherTextBase64Url) {
        try {
            byte[] combined = Base64.getUrlDecoder().decode(cipherTextBase64Url);
            if (combined.length < 12 + 16) {
                throw new IllegalArgumentException("Ciphertext too short for AES-GCM");
            }

            ByteBuffer buffer = ByteBuffer.wrap(combined);
            byte[] iv = new byte[12];
            buffer.get(iv);

            byte[] cipherTextWithTag = new byte[buffer.remaining()];
            buffer.get(cipherTextWithTag);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(getEncryptionKeyBytes(), "AES");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);

            byte[] plainTextBytes = cipher.doFinal(cipherTextWithTag);
            return new String(plainTextBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("AES-GCM decryption failed: " + e.getMessage(), e);
        }
    }

    /**
     * Generates a signed JWT with AES-256-GCM encrypted payload claims in "enc_data".
     */
    public String generateToken(UserEntity user) {
        try {
            Map<String, Object> userData = new HashMap<>();
            userData.put("userId", user.getId());
            userData.put("name", user.getName());
            userData.put("email", user.getEmail());
            userData.put("role", user.getRole());
            if (user.getEnrolmentNumber() != null) {
                userData.put("enrolmentNumber", user.getEnrolmentNumber());
            }
            if (user.getClassId() != null) {
                userData.put("classId", user.getClassId());
            }

            String jsonPayload = objectMapper.writeValueAsString(userData);
            String encryptedData = encryptPayload(jsonPayload);

            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + expirationMs);

            Map<String, Object> claims = new HashMap<>();
            // Store encrypted payload: jwt.io will only see ciphertext in "enc_data"
            claims.put("enc_data", encryptedData);

            return Jwts.builder()
                    .subject(user.getId())
                    .claims(claims)
                    .issuedAt(now)
                    .expiration(expiryDate)
                    .signWith(getSigningKey())
                    .compact();
        } catch (Exception e) {
            throw new RuntimeException("Token generation failed: " + e.getMessage(), e);
        }
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUserId(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Decrypts and returns the user claims map from "enc_data" in the JWT.
     */
    public Map<String, Object> extractDecryptedUserClaims(String token) {
        Claims claims = extractAllClaims(token);
        Object encData = claims.get("enc_data");
        if (encData != null) {
            try {
                String decryptedJson = decryptPayload(encData.toString());
                return objectMapper.readValue(decryptedJson, new TypeReference<Map<String, Object>>() {});
            } catch (Exception e) {
                // fall through
            }
        }

        // Backward compatibility if token has plain unencrypted claims
        Map<String, Object> fallback = new HashMap<>();
        claims.forEach(fallback::put);
        return fallback;
    }

    public String extractRole(String token) {
        Map<String, Object> decrypted = extractDecryptedUserClaims(token);
        Object role = decrypted.get("role");
        return role != null ? role.toString() : null;
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public ResponseCookie createHttpOnlyCookie(String token) {
        return ResponseCookie.from(cookieName, token)
                .httpOnly(true)
                .secure(false) // Set to false for localhost HTTP development
                .path("/")
                .maxAge(expirationMs / 1000)
                .sameSite("Lax")
                .build();
    }

    public ResponseCookie createClientCookie(String token) {
        return ResponseCookie.from("client_jwt", token)
                .httpOnly(false) // Readable by client JavaScript document.cookie
                .secure(false)
                .path("/")
                .maxAge(expirationMs / 1000)
                .sameSite("Lax")
                .build();
    }

    public ResponseCookie createCleanHttpOnlyCookie() {
        return ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
    }

    public ResponseCookie createCleanClientCookie() {
        return ResponseCookie.from("client_jwt", "")
                .httpOnly(false)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
    }

    public String getCookieName() {
        return cookieName;
    }

    public String getPayloadEncryptionKey() {
        return payloadEncryptionKey;
    }
}
