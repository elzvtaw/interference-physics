package com.example.isib.interference.web.api;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;

@RestController
@RequestMapping("/api/auth")
public class InterferenceAuthController {

    private static final String SECRET = "mySuperSecretKeyForJWTThatIsAtLeast32CharactersLong2025";

    @PostMapping("/Interference")
    public LoginResponse login(@RequestBody LoginRequest request) {
        // Проверка логина и пароля
        if ("user".equals(request.getUsername()) && "password".equals(request.getPassword())) {

            SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
            String token = Jwts.builder()
                    .setSubject(request.getUsername())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                    .signWith(key)
                    .compact();

            LoginResponse response = new LoginResponse();
            response.setToken(token);
            return response;
        }

        throw new RuntimeException("Invalid credentials");
    }

    static class LoginRequest {
        private String username;
        private String password;
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    static class LoginResponse {
        private String token;
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }
}