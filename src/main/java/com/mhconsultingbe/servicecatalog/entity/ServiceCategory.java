package com.mhconsultingbe.servicecatalog.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.mhconsultingbe.shared.exception.ResourceNotFoundException;

import java.util.Arrays;

public enum ServiceCategory {
    THANH_LAP("thanh-lap"), KE_TOAN("ke-toan"), THUE("thue"), KHAC("khac");

    private final String apiValue;
    ServiceCategory(String apiValue) { this.apiValue = apiValue; }
    @JsonValue
    public String apiValue() {
        return apiValue;
    }

    @JsonCreator
    public static ServiceCategory fromApiValue(String value) {
        return Arrays.stream(values()).filter(v -> v.apiValue.equalsIgnoreCase(value)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported service category: " + value));
    }
}
