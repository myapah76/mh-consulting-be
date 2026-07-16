package com.mhconsultingbe.servicecatalog.dto.request;

import jakarta.validation.constraints.NotNull;

public record ActivePatchRequest(
        @NotNull
        Boolean active
) {
}
