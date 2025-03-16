package com.logify.processor.auth.security;

import com.logify.processor.auth.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = request.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        token = token.substring(7);
        try {
            String username = jwtUtil.extractUsername(token);
            UserDetails userDetails = userRepository.findByUsername(username)
                    .map(user -> new User(user.getUsername(), user.getPassword(), Collections.emptyList()))
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            if (jwtUtil.validateToken(token, username)) {
                // ✅ FIX: Use UsernamePasswordAuthenticationToken instead of JwtAuthenticationToken
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (ExpiredJwtException | MalformedJwtException | UnsupportedJwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
