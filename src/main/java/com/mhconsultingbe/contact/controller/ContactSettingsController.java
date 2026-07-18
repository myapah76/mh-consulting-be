package com.mhconsultingbe.contact.controller;

import com.mhconsultingbe.contact.dto.request.ContactSettingsRequest;
import com.mhconsultingbe.contact.dto.response.ContactSettingsResponse;
import com.mhconsultingbe.contact.service.ContactSettingsOperations;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ContactSettingsController {
    private final ContactSettingsOperations contactSettingsOperations;

    @GetMapping("/api/public/contact-settings")
    public ContactSettingsResponse publicSettings() {
        return contactSettingsOperations.get();
    }

    @GetMapping("/api/admin/contact-settings")
    public ContactSettingsResponse adminSettings() {
        return contactSettingsOperations.get();
    }

    @PutMapping("/api/admin/contact-settings")
    public ContactSettingsResponse update(
            @Valid
            @RequestBody
            ContactSettingsRequest request
    ) {
        return contactSettingsOperations.update(request);
    }
}
