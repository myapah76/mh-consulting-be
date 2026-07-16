package com.mhconsultingbe.servicecatalog.controller;

import com.mhconsultingbe.servicecatalog.dto.response.ServiceCategoryDeleteResponse;
import com.mhconsultingbe.servicecatalog.service.ServiceCategoryOperations;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminServiceCategoryControllerTests {
    private final ServiceCategoryOperations service = mock(ServiceCategoryOperations.class);
    private final AdminServiceCategoryController controller = new AdminServiceCategoryController(service);

    @Test
    void deleteUnusedCategoryReturnsNoContent() {
        UUID id = UUID.randomUUID();
        when(service.delete(id)).thenReturn(
                new ServiceCategoryDeleteResponse(true, false, "Category deleted")
        );

        var response = controller.delete(id);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void deleteReferencedCategoryReturnsResult() {
        UUID id = UUID.randomUUID();
        var result = new ServiceCategoryDeleteResponse(
                false,
                false,
                "Referenced category was deactivated"
        );
        when(service.delete(id)).thenReturn(result);

        var response = controller.delete(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(result, response.getBody());
    }
}
