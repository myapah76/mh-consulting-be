package com.mhconsultingbe.contact.controller;

import com.mhconsultingbe.contact.dto.*;
import com.mhconsultingbe.contact.service.ContactSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController @RequiredArgsConstructor
public class ContactSettingsController {
    private final ContactSettingsService service;
    @GetMapping("/api/public/contact-settings") ContactSettingsResponse publicSettings() { return service.get(); }
    @GetMapping("/api/admin/contact-settings") ContactSettingsResponse adminSettings() { return service.get(); }
    @PutMapping("/api/admin/contact-settings") ContactSettingsResponse update(@Valid @RequestBody ContactSettingsRequest body) { return service.update(body); }
}
