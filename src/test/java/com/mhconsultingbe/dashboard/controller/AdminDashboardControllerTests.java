package com.mhconsultingbe.dashboard.controller;

import com.mhconsultingbe.dashboard.dto.response.DashboardPeriodResponse;
import com.mhconsultingbe.dashboard.dto.response.DashboardResponse;
import com.mhconsultingbe.dashboard.dto.response.DashboardSummaryResponse;
import com.mhconsultingbe.dashboard.service.DashboardOperations;
import com.mhconsultingbe.shared.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AdminDashboardController.class,
        properties = "app.frontend-url=http://localhost:3000"
)
@Import(SecurityConfig.class)
class AdminDashboardControllerTests {
    private static final Instant FROM = Instant.parse("2026-07-12T17:00:00Z");
    private static final Instant TO = Instant.parse("2026-07-19T16:59:59.999Z");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardOperations dashboardOperations;

    @Test
    void authenticatedAdminReceivesDashboardContract() throws Exception {
        when(dashboardOperations.getDashboard(FROM, TO)).thenReturn(response());

        mockMvc.perform(get("/api/admin/dashboard")
                        .queryParam("from", FROM.toString())
                        .queryParam("to", TO.toString())
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.period.from").value("2026-07-12T17:00:00Z"))
                .andExpect(jsonPath("$.period.to").value("2026-07-19T16:59:59.999Z"))
                .andExpect(jsonPath("$.period.timezone").value("Asia/Ho_Chi_Minh"))
                .andExpect(jsonPath("$.summary.total").value(0))
                .andExpect(jsonPath("$.summary.completionRate").value(0.0))
                .andExpect(jsonPath("$.dailyConsultations").isArray())
                .andExpect(jsonPath("$.consultationsByService").isArray())
                .andExpect(jsonPath("$.consultationsByCategory").isArray())
                .andExpect(jsonPath("$.recentConsultations").isArray());

        verify(dashboardOperations).getDashboard(FROM, TO);
    }

    @Test
    void omittedRangeIsDelegatedForDefaultWeekResolution() throws Exception {
        when(dashboardOperations.getDashboard(null, null)).thenReturn(response());

        mockMvc.perform(get("/api/admin/dashboard")
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk());

        verify(dashboardOperations).getDashboard(null, null);
    }

    @Test
    void unauthenticatedAccessReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void nonAdminAccessReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard")
                        .with(user("user@example.com").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void malformedInstantReturnsInvalidParameterError() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard")
                        .queryParam("from", "not-an-instant")
                        .queryParam("to", TO.toString())
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));
    }

    private DashboardResponse response() {
        return new DashboardResponse(
                new DashboardPeriodResponse(FROM, TO, "Asia/Ho_Chi_Minh"),
                new DashboardSummaryResponse(0, 0, 0, 0, 0, 0.0),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );
    }
}
