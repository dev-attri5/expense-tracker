package com.expense;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserDAO userDAO;

    public AuthController(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Check current login session status.
     * Used by front-end pages to guard routes and display user greeting.
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpSession session) {
        Long userId = (Long) session.getAttribute("USER_ID");
        String username = (String) session.getAttribute("USERNAME");

        Map<String, Object> response = new HashMap<>();
        if (userId != null && username != null) {
            response.put("loggedIn", true);
            response.put("userId", userId);
            response.put("username", username);
            return ResponseEntity.ok(response);
        } else {
            response.put("loggedIn", false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    /**
     * Register a new user account.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request, HttpSession session) {
        String username = request.get("username");
        String password = request.get("password");

        // 1. Basic validation
        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Username is required.");
        }
        username = username.trim().toLowerCase();

        if (username.length() < 3 || username.length() > 30) {
            return ResponseEntity.badRequest().body("Username must be between 3 and 30 characters.");
        }

        if (password == null || password.trim().length() < 4) {
            return ResponseEntity.badRequest().body("Password must be at least 4 characters long.");
        }

        // 2. Check if username is already taken
        if (userDAO.existsByUsername(username)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already taken. Please choose another.");
        }

        // 3. Hash password and insert into database
        try {
            String hashedPassword = PasswordUtil.hashPassword(password.trim());
            int rowsAffected = userDAO.createUser(username, hashedPassword);

            if (rowsAffected > 0) {
                User created = userDAO.findByUsername(username);
                if (created != null) {
                    session.setAttribute("USER_ID", created.getId());
                    session.setAttribute("USERNAME", created.getUsername());
                }

                Map<String, Object> response = new HashMap<>();
                response.put("message", "Account created successfully.");
                response.put("username", username);
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create user account.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Database error: " + e.getMessage());
        }
    }

    /**
     * Log in with username and password.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request, HttpSession session) {
        String username = request.get("username");
        String password = request.get("password");

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Username and password are required.");
        }
        username = username.trim().toLowerCase();

        try {
            User user = userDAO.findByUsername(username);
            if (user == null || !PasswordUtil.checkPassword(password.trim(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password.");
            }

            // Store user identity in the HTTP session
            session.setAttribute("USER_ID", user.getId());
            session.setAttribute("USERNAME", user.getUsername());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful.");
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Database error: " + e.getMessage());
        }
    }

    /**
     * Log out and destroy session.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        try {
            session.invalidate();
        } catch (Exception ignored) {}

        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully.");
        return ResponseEntity.ok(response);
    }
}
