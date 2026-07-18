package com.mhconsultingbe.emailsettings.service;

import com.mhconsultingbe.auth.dto.response.MessageResponse;
import com.mhconsultingbe.emailsettings.dto.request.EmailSettingsUpdateRequest;
import com.mhconsultingbe.emailsettings.dto.request.TestEmailRequest;
import com.mhconsultingbe.emailsettings.dto.response.EmailSettingsResponse;

public interface EmailSettingsOperations {
    EmailSettingsResponse get();

    EmailSettingsResponse update(EmailSettingsUpdateRequest request);

    MessageResponse sendTestEmail(TestEmailRequest request);
}
