package com.mhconsultingbe.contact.service;

import com.mhconsultingbe.contact.dto.request.ContactSettingsRequest;
import com.mhconsultingbe.contact.dto.response.ContactSettingsResponse;

public interface ContactSettingsOperations {
    ContactSettingsResponse get();

    ContactSettingsResponse update(ContactSettingsRequest request);
}
