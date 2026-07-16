package com.mhconsultingbe.servicecatalog.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceCategoryTests {
    @Test
    void defaultsToActiveWithZeroDisplayOrder() {
        ServiceCategory category = new ServiceCategory();

        assertTrue(category.isActive());
        assertEquals(0, category.getDisplayOrder());
    }
}
