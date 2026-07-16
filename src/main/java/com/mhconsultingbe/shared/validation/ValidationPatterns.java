package com.mhconsultingbe.shared.validation;

public final class ValidationPatterns {
    public static final String STRONG_PASSWORD =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s]).{8,200}$";
    public static final String SLUG = "^[a-z0-9]+(?:-[a-z0-9]+)*$";
    public static final String VIETNAMESE_PHONE = "^(?:\\+?84|0)[\\s.-]?(?:3|5|7|8|9)(?:[\\s.-]?\\d){8}$";
    private ValidationPatterns() {}
}
