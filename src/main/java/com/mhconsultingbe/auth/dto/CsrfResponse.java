package com.mhconsultingbe.auth.dto;

public record CsrfResponse(String headerName, String parameterName, String token) {}
