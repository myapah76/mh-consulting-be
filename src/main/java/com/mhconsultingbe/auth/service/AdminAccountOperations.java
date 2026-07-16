package com.mhconsultingbe.auth.service;

import com.mhconsultingbe.auth.dto.request.AdminAccountCreateRequest;
import com.mhconsultingbe.auth.dto.request.ChangePasswordRequest;
import com.mhconsultingbe.auth.dto.response.AdminAccountResponse;

public interface AdminAccountOperations {
    AdminAccountResponse create(AdminAccountCreateRequest request);

    void changePassword(String authenticatedEmail, ChangePasswordRequest request);
}
