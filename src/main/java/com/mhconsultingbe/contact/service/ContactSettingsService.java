package com.mhconsultingbe.contact.service;

import com.mhconsultingbe.contact.dto.ContactSettingsRequest;
import com.mhconsultingbe.contact.dto.ContactSettingsResponse;

public interface ContactSettingsService {
    ContactSettingsResponse get();

    ContactSettingsResponse update(ContactSettingsRequest body);
}
