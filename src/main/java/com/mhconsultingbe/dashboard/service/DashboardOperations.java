package com.mhconsultingbe.dashboard.service;

import com.mhconsultingbe.dashboard.dto.response.DashboardResponse;

import java.time.Instant;

public interface DashboardOperations {
    DashboardResponse getDashboard(Instant from, Instant to);
}
