package com.mhconsultingbe.auth.service;

import com.mhconsultingbe.auth.dto.AdminResponse;
import com.mhconsultingbe.auth.dto.LoginRequest;
import com.mhconsultingbe.auth.repository.AdminRepository;
import com.mhconsultingbe.shared.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final AdminRepository adminRepository;

    @Override
    public AdminResponse login(LoginRequest body, HttpServletRequest request, HttpServletResponse response) {
        var authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(body.email().trim().toLowerCase(), body.password()));
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
        return current(authentication);
    }

    @Override
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        var session = request.getSession(false);
        if (session != null) session.invalidate();
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @Override
    public AdminResponse current(Authentication authentication) {
        var admin = adminRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated admin no longer exists"));
        return new AdminResponse(admin.getId(), admin.getEmail(), admin.getFullName(), admin.getRole().name());
    }
}
