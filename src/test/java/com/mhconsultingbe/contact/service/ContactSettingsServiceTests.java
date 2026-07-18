package com.mhconsultingbe.contact.service;

import com.mhconsultingbe.contact.dto.request.ContactSettingsRequest;
import com.mhconsultingbe.contact.entity.ContactSettings;
import com.mhconsultingbe.contact.repository.ContactSettingsRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ContactSettingsServiceTests {
    private final ContactSettingsRepository repository = mock(ContactSettingsRepository.class);
    private final ContactSettingsService service = new ContactSettingsService(repository);

    @Test
    void updatePersistsExistingRowAndNormalizesValues() {
        ContactSettings existing = settings();
        when(repository.findBySingletonKeyTrue()).thenReturn(Optional.of(existing));
        when(repository.save(any(ContactSettings.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.update(new ContactSettingsRequest(
                " Address ",
                " 0903.024.116 ",
                " Ms. Thảo ",
                " ",
                " Must be removed ",
                " INFO@MHCONSULTING.VN ",
                " Working hours ",
                " ",
                " https://zalo.me/0903024116 ",
                null
        ));

        ArgumentCaptor<ContactSettings> saved = ArgumentCaptor.forClass(ContactSettings.class);
        verify(repository).save(saved.capture());
        assertSame(existing, saved.getValue());
        assertEquals("Address", response.address());
        assertEquals("0903.024.116", response.primaryPhone());
        assertEquals("info@mhconsulting.vn", response.email());
        assertNull(response.secondaryPhone());
        assertNull(response.secondaryPhoneLabel());
        assertNull(response.facebookUrl());
        assertNull(response.youtubeUrl());
    }

    @Test
    void publicStyleRetrievalImmediatelyReturnsPersistedUpdate() {
        ContactSettings existing = settings();
        when(repository.findBySingletonKeyTrue()).thenReturn(Optional.of(existing));
        when(repository.save(any(ContactSettings.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var updated = service.update(validRequest());
        var retrieved = service.get();

        assertEquals(updated, retrieved);
        verify(repository, times(2)).findBySingletonKeyTrue();
    }

    private ContactSettingsRequest validRequest() {
        return new ContactSettingsRequest(
                "New address",
                "0903.024.116",
                "Ms. Thảo",
                "0938.835.633",
                "Mr. Trí",
                "info@mhconsulting.vn",
                "Thứ 2 - Thứ 7: 08:00 - 17:30",
                null,
                "https://zalo.me/0903024116",
                null
        );
    }

    private ContactSettings settings() {
        ContactSettings settings = new ContactSettings();
        settings.setAddress("Old address");
        settings.setPrimaryPhone("0900000000");
        settings.setEmail("old@example.com");
        settings.setWorkingHours("Old hours");
        return settings;
    }
}
