package com.mhconsultingbe.servicecatalog.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceCategoryTests {
    @Test
    void defaultsToActive() {
        ServiceCategory category = new ServiceCategory();

        assertTrue(category.isActive());
    }
}
