package com.mhconsultingbe.shared.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.Set;

public final class PageableFactory {
    private PageableFactory() {}

    public static Pageable create(int page, int size, String[] sort, Set<String> allowed, Sort fallback) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 100);
        if (sort == null || sort.length == 0) return PageRequest.of(safePage, safeSize, fallback);
        var orders = Arrays.stream(sort).map(value -> value.split(",", 2)).map(parts -> {
            if (!allowed.contains(parts[0])) throw new IllegalArgumentException("Unsupported sort field: " + parts[0]);
            var direction = parts.length == 2 ? Sort.Direction.fromString(parts[1]) : Sort.Direction.ASC;
            return new Sort.Order(direction, parts[0]);
        }).toList();
        return PageRequest.of(safePage, safeSize, Sort.by(orders));
    }
}
