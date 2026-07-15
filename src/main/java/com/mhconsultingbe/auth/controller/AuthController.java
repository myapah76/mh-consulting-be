package com.mhconsultingbe.auth.controller;

import com.mhconsultingbe.auth.dto.AdminResponse;
import com.mhconsultingbe.auth.dto.CsrfResponse;
import com.mhconsultingbe.auth.dto.LoginRequest;
import com.mhconsultingbe.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService service;

    @GetMapping("/csrf")
    CsrfResponse csrf(CsrfToken token) {
        return new CsrfResponse(token.getHeaderName(), token.getParameterName(), token.getToken());
    }

    @PostMapping("/login")
    AdminResponse login(
            @Valid
            @RequestBody
            LoginRequest body,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        return service.login(body, request, response);
    }

    @PostMapping("/logout")
    ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        return service.logout(request);
    }

    @GetMapping("/me")
    AdminResponse me(Authentication authentication) {
        return service.current(authentication);
    }
}
