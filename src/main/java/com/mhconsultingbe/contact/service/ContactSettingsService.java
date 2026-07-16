package com.mhconsultingbe.contact.service;

import com.mhconsultingbe.contact.dto.request.ContactSettingsRequest;
import com.mhconsultingbe.contact.dto.response.ContactSettingsResponse;
import com.mhconsultingbe.contact.entity.ContactSettings;
import com.mhconsultingbe.contact.mapper.ContactSettingsMapper;
import com.mhconsultingbe.contact.repository.ContactSettingsRepository;
import com.mhconsultingbe.shared.exception.ResourceNotFoundException;
import com.mhconsultingbe.shared.util.TextNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class ContactSettingsService {
    private final ContactSettingsRepository repository;
    @Transactional(readOnly = true)
    public ContactSettingsResponse get() { return ContactSettingsMapper.response(required()); }
    @Transactional
    public ContactSettingsResponse update(ContactSettingsRequest b) {
        var s = repository.findBySingletonKeyTrue().orElseGet(ContactSettings::new);
        s.setCompanyName(TextNormalizer.plainText(b.companyName())); s.setTagline(TextNormalizer.plainText(b.tagline()));
        s.setPrimaryPhone(TextNormalizer.trimToNull(b.primaryPhone())); s.setHotline1(TextNormalizer.trimToNull(b.hotline1()));
        s.setHotline2(TextNormalizer.trimToNull(b.hotline2())); s.setHotline3(TextNormalizer.trimToNull(b.hotline3()));
        s.setEmail(TextNormalizer.lowercase(b.email())); s.setAddress(TextNormalizer.plainText(b.address()));
        s.setZaloUrl(TextNormalizer.trimToNull(b.zaloUrl())); s.setFacebookUrl(TextNormalizer.trimToNull(b.facebookUrl()));
        s.setGoogleMapsUrl(TextNormalizer.trimToNull(b.googleMapsUrl()));
        s.setBusinessRegistrationText(TextNormalizer.plainText(b.businessRegistrationText()));
        return ContactSettingsMapper.response(repository.save(s));
    }
    private ContactSettings required() { return repository.findBySingletonKeyTrue().orElseThrow(() -> new ResourceNotFoundException("Contact settings not configured")); }
}
