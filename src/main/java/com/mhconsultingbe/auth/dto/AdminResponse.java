package com.mhconsultingbe.auth.dto;

import java.util.UUID;

public record AdminResponse(UUID id, String email, String fullName, String role) {}
