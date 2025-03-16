package com.logify.processor.auth.controller;

import com.logify.processor.auth.model.User;
import com.logify.processor.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        String token = authService.registerUser(username, password, Set.of("USER")); // Default role

        return ResponseEntity.ok(Map.of("message", "User registered successfully", "token", token));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        String token = authService.loginUser(username, password);

        return ResponseEntity.ok(Map.of("message", "Login successful", "token", token));
    }
}
