package com.mhconsultingbe.contact.controller;

import com.mhconsultingbe.contact.dto.ContactSettingsRequest;
import com.mhconsultingbe.contact.dto.ContactSettingsResponse;
import com.mhconsultingbe.contact.service.ContactSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ContactSettingsController {
    private final ContactSettingsService service;

    @GetMapping("/api/public/contact-settings")
    ContactSettingsResponse publicSettings() {
        return service.get();
    }

    @GetMapping("/api/admin/contact-settings")
    ContactSettingsResponse adminSettings() {
        return service.get();
    }

    @PutMapping("/api/admin/contact-settings")
    ContactSettingsResponse update(
            @Valid
            @RequestBody
            ContactSettingsRequest body
    ) {
        return service.update(body);
    }
}
