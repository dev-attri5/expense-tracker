package com.expense;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Hashes a plain-text password using SHA-256 and a random cryptographic salt.
     * Stored in the format "salt:hash".
     */
    public static String hashPassword(String password) {
        try {
            byte[] salt = new byte[16];
            RANDOM.nextBytes(salt);

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes(StandardCharsets.UTF_8));

            String saltBase64 = Base64.getEncoder().encodeToString(salt);
            String hashBase64 = Base64.getEncoder().encodeToString(hashedPassword);

            return saltBase64 + ":" + hashBase64;
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Checks if a raw password matches the stored salted hash.
     */
    public static boolean checkPassword(String rawPassword, String storedHash) {
        try {
            if (storedHash == null || !storedHash.contains(":")) {
                return false;
            }

            String[] parts = storedHash.split(":");
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[1]);

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] actualHash = md.digest(rawPassword.getBytes(StandardCharsets.UTF_8));

            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (Exception e) {
            return false;
        }
    }
}
