package com.mhconsultingbe.auth.controller;

import com.mhconsultingbe.auth.dto.request.AdminAccountCreateRequest;
import com.mhconsultingbe.auth.dto.request.ChangePasswordRequest;
import com.mhconsultingbe.auth.dto.response.AdminAccountResponse;
import com.mhconsultingbe.auth.dto.response.MessageResponse;
import com.mhconsultingbe.auth.service.AdminAccountOperations;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/accounts")
@RequiredArgsConstructor
public class AdminAccountController {
    private final AdminAccountOperations service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    AdminAccountResponse create(
            @Valid
            @RequestBody
            AdminAccountCreateRequest request
    ) {
        return service.create(request);
    }

    @PutMapping("/me/password")
    MessageResponse changePassword(
            Authentication authentication,
            @Valid
            @RequestBody
            ChangePasswordRequest request
    ) {
        service.changePassword(authentication.getName(), request);
        return new MessageResponse("Password changed successfully");
    }
}
