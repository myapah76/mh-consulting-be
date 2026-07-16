package com.mhconsultingbe.contact.mapper;

import com.mhconsultingbe.contact.dto.response.ContactSettingsResponse;
import com.mhconsultingbe.contact.entity.ContactSettings;

public final class ContactSettingsMapper {
    private ContactSettingsMapper() {}
    public static ContactSettingsResponse response(ContactSettings s) {
        return new ContactSettingsResponse(s.getId(), s.getCompanyName(), s.getTagline(), s.getPrimaryPhone(),
                s.getHotline1(), s.getHotline2(), s.getHotline3(), s.getEmail(), s.getAddress(), s.getZaloUrl(),
                s.getFacebookUrl(), s.getGoogleMapsUrl(), s.getBusinessRegistrationText(), s.getUpdatedAt());
    }
}
