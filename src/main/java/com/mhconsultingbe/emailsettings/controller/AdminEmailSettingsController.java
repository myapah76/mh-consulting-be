package com.mhconsultingbe.emailsettings.controller;

import com.mhconsultingbe.auth.dto.response.MessageResponse;
import com.mhconsultingbe.emailsettings.dto.request.EmailSettingsUpdateRequest;
import com.mhconsultingbe.emailsettings.dto.request.TestEmailRequest;
import com.mhconsultingbe.emailsettings.dto.response.EmailSettingsResponse;
import com.mhconsultingbe.emailsettings.service.EmailSettingsOperations;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/email-settings")
@RequiredArgsConstructor
public class AdminEmailSettingsController {
    private final EmailSettingsOperations emailSettingsOperations;

    @GetMapping
    public EmailSettingsResponse get(HttpServletResponse response) {
        noStore(response);
        return emailSettingsOperations.get();
    }

    @PutMapping
    public EmailSettingsResponse update(
            @Valid
            @RequestBody
            EmailSettingsUpdateRequest request,
            HttpServletResponse response
    ) {
        noStore(response);
        return emailSettingsOperations.update(request);
    }

    @PostMapping("/test")
    public MessageResponse test(
            @Valid
            @RequestBody
            TestEmailRequest request,
            HttpServletResponse response
    ) {
        noStore(response);
        return emailSettingsOperations.sendTestEmail(request);
    }

    private void noStore(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
    }
}
