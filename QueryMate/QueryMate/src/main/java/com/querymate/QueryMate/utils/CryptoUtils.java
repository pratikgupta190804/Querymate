package com.querymate.QueryMate.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class CryptoUtils {

    private final String SECRET_KEY;

    public CryptoUtils(@Value("${app.crypto.secret}") String secretKey) {
        this.SECRET_KEY = secretKey;
    }

    public String encrypt(String plainText) {
        try {
            // Real encryption logic (replace with AES or secure algorithm)
            return Base64.getEncoder().encodeToString(plainText.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting: " + e.getMessage(), e);
        }
    }

    public String decrypt(String encryptedText) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
            return new String(decodedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting: " + e.getMessage(), e);
        }
    }
}
