package com.mhconsultingbe.servicecatalog.dto;

import jakarta.validation.constraints.NotNull;

public record ActivePatchRequest(@NotNull Boolean active) {}
