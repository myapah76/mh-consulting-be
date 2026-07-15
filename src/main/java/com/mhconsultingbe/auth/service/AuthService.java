package com.mhconsultingbe.auth.service;

import com.mhconsultingbe.auth.dto.AdminResponse;
import com.mhconsultingbe.auth.dto.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.Map;

public interface AuthService {
    AdminResponse login(LoginRequest body, HttpServletRequest request, HttpServletResponse response);

    ResponseEntity<Map<String, String>> logout(HttpServletRequest request);

    AdminResponse current(Authentication authentication);
}
