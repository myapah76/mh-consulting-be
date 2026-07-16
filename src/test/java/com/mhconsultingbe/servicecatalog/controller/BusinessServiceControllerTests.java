package com.mhconsultingbe.servicecatalog.controller;

import com.mhconsultingbe.servicecatalog.service.BusinessServiceOperations;
import com.mhconsultingbe.shared.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = PublicServiceController.class,
        properties = "app.frontend-url=http://localhost:3000"
)
@Import(SecurityConfig.class)
class BusinessServiceControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BusinessServiceOperations service;

    @Test
    void removedDisplayOrderSortReturnsInvalidParameterResponse() throws Exception {
        when(service.list(
                isNull(),
                eq(true),
                anyInt(),
                anyInt(),
                eq("displayOrder,asc")
        )).thenThrow(new IllegalArgumentException("Unsupported sort field: displayOrder"));

        mockMvc.perform(get("/api/public/services")
                        .param("sort", "displayOrder,asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"))
                .andExpect(jsonPath("$.message").value("A query or path parameter is invalid"));
    }
}
