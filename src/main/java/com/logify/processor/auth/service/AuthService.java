package com.logify.processor.auth.service;

import com.logify.processor.auth.model.User;
import com.logify.processor.auth.repository.UserRepository;
import com.logify.processor.auth.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public String registerUser(String username, String password, Set<String> roles) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("User already exists!");
        }
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .roles(roles)
                .build();
        userRepository.save(user);
        return jwtUtil.generateToken(username);
    }

    public String loginUser(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }
        return jwtUtil.generateToken(username);
    }
}
