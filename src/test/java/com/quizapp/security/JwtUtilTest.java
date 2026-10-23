package com.quizapp.security;

import com.quizapp.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    public void setup() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "quiz_app_super_secret_jwt_hmac_sha256_key_for_testing_purpose_only_12345");
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", 3600000L); // 1 hour
        ReflectionTestUtils.setField(jwtUtil, "cookieName", "jwt_token");
        ReflectionTestUtils.setField(jwtUtil, "payloadEncryptionKey", "quiz_app_aes_256_gcm_payload_secret_key_32bytes!!");
    }

    @Test
    public void testAesGcmPayloadEncryptionAndDecryption() {
        String plain = "{\"role\":\"teacher\",\"secretInfo\":\"confidential123\"}";
        String encrypted = jwtUtil.encryptPayload(plain);

        assertNotNull(encrypted);
        assertNotEquals(plain, encrypted);

        String decrypted = jwtUtil.decryptPayload(encrypted);
        assertEquals(plain, decrypted);
    }

    @Test
    public void testGenerateTokenWithEncryptedPayload() {
        UserEntity user = new UserEntity();
        user.setId("usr-teacher-sarah");
        user.setName("Dr. Sarah Jenkins");
        user.setEmail("sarah.jenkins@oakridge.edu");
        user.setRole("teacher");

        String token = jwtUtil.generateToken(user);
        assertNotNull(token);
        assertFalse(token.isBlank());

        // Validate token signature and expiration
        assertTrue(jwtUtil.validateToken(token));
        assertEquals("usr-teacher-sarah", jwtUtil.extractUserId(token));

        // Ensure claims in the JWT contains enc_data (ciphertext)
        assertNotNull(jwtUtil.extractAllClaims(token).get("enc_data"));

        // Ensure the decrypted user claims match the original user
        Map<String, Object> decryptedClaims = jwtUtil.extractDecryptedUserClaims(token);
        assertEquals("usr-teacher-sarah", decryptedClaims.get("userId"));
        assertEquals("Dr. Sarah Jenkins", decryptedClaims.get("name"));
        assertEquals("sarah.jenkins@oakridge.edu", decryptedClaims.get("email"));
        assertEquals("teacher", decryptedClaims.get("role"));
        assertEquals("teacher", jwtUtil.extractRole(token));
    }

    @Test
    public void testCookiesCreation() {
        String token = "dummy.jwt.token";

        ResponseCookie httpOnlyCookie = jwtUtil.createHttpOnlyCookie(token);
        assertEquals("jwt_token", httpOnlyCookie.getName());
        assertEquals(token, httpOnlyCookie.getValue());
        assertTrue(httpOnlyCookie.isHttpOnly());
        assertEquals("/", httpOnlyCookie.getPath());
        assertEquals("Lax", httpOnlyCookie.getSameSite());

        ResponseCookie clientCookie = jwtUtil.createClientCookie(token);
        assertEquals("client_jwt", clientCookie.getName());
        assertEquals(token, clientCookie.getValue());
        assertFalse(clientCookie.isHttpOnly());

        ResponseCookie cleanHttpOnly = jwtUtil.createCleanHttpOnlyCookie();
        assertEquals(0, cleanHttpOnly.getMaxAge().getSeconds());

        ResponseCookie cleanClient = jwtUtil.createCleanClientCookie();
        assertEquals(0, cleanClient.getMaxAge().getSeconds());
    }
}
