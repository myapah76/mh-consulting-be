package com.mhconsultingbe.auth.dto.response;

public record CsrfResponse(
        String headerName,
        String parameterName,
        String token
) {
}
