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

@Service
@RequiredArgsConstructor
public class ContactSettingsService implements ContactSettingsOperations {
    private final ContactSettingsRepository repository;

    @Override
    @Transactional(readOnly = true)
    public ContactSettingsResponse get() {
        return ContactSettingsMapper.response(required());
    }

    @Override
    @Transactional
    public ContactSettingsResponse update(ContactSettingsRequest request) {
        ContactSettings settings = required();
        settings.setAddress(TextNormalizer.plainText(request.address()));
        settings.setPrimaryPhone(TextNormalizer.trimToNull(request.primaryPhone()));
        settings.setPrimaryPhoneLabel(TextNormalizer.plainText(request.primaryPhoneLabel()));
        settings.setSecondaryPhone(TextNormalizer.trimToNull(request.secondaryPhone()));
        settings.setSecondaryPhoneLabel(settings.getSecondaryPhone() == null
                ? null
                : TextNormalizer.plainText(request.secondaryPhoneLabel()));
        settings.setEmail(TextNormalizer.lowercase(request.email()));
        settings.setWorkingHours(TextNormalizer.plainText(request.workingHours()));
        settings.setFacebookUrl(TextNormalizer.trimToNull(request.facebookUrl()));
        settings.setZaloUrl(TextNormalizer.trimToNull(request.zaloUrl()));
        settings.setYoutubeUrl(TextNormalizer.trimToNull(request.youtubeUrl()));
        return ContactSettingsMapper.response(repository.save(settings));
    }

    private ContactSettings required() {
        return repository.findBySingletonKeyTrue()
                .orElseThrow(() -> new ResourceNotFoundException("Contact settings not configured"));
    }
}
