package com.mhconsultingbe.contact.mapper;

import com.mhconsultingbe.contact.dto.response.ContactSettingsResponse;
import com.mhconsultingbe.contact.entity.ContactSettings;

public final class ContactSettingsMapper {
    private ContactSettingsMapper() {
    }

    public static ContactSettingsResponse response(ContactSettings settings) {
        return new ContactSettingsResponse(
                settings.getAddress(),
                settings.getPrimaryPhone(),
                settings.getPrimaryPhoneLabel(),
                settings.getSecondaryPhone(),
                settings.getSecondaryPhoneLabel(),
                settings.getEmail(),
                settings.getWorkingHours(),
                settings.getFacebookUrl(),
                settings.getZaloUrl(),
                settings.getYoutubeUrl()
        );
    }
}
