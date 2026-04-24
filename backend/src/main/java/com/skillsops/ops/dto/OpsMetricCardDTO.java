package com.skillsops.ops.dto;

public record OpsMetricCardDTO(
        String key,
        String label,
        long value) {
}
