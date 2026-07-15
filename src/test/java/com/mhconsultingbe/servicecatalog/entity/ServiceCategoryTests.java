package com.mhconsultingbe.servicecatalog.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ServiceCategoryTests {
    @Test void mapsFrontendApiValues() {
        assertEquals(ServiceCategory.THANH_LAP, ServiceCategory.fromApiValue("thanh-lap"));
        assertEquals("ke-toan", ServiceCategory.KE_TOAN.apiValue());
        assertThrows(IllegalArgumentException.class, () -> ServiceCategory.fromApiValue("invalid"));
    }
}
