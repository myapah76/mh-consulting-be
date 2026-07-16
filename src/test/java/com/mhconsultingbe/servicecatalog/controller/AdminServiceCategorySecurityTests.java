package com.mhconsultingbe.servicecatalog.controller;

import com.mhconsultingbe.servicecatalog.service.ServiceCategoryOperations;
import com.mhconsultingbe.shared.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AdminServiceCategoryController.class,
        properties = "app.frontend-url=http://localhost:3000"
)
@Import(SecurityConfig.class)
class AdminServiceCategorySecurityTests {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ServiceCategoryOperations service;

    @Test
    void unauthenticatedAdminAccessReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/service-categories"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void nonAdminAccessReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/service-categories")
                        .with(user("user").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void unsafeAdminRequestWithoutCsrfReturnsForbidden() throws Exception {
        mockMvc.perform(post("/api/admin/service-categories")
                        .with(user("admin").roles("ADMIN"))
                        .contentType("application/json")
                        .content("""
                                {
                                  "slug": "category",
                                  "name": "Category"
                                }
                                """))
                .andExpect(status().isForbidden());
    }
}
