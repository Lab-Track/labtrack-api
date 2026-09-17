package com.labtrack.labtrack.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record StudentResponse(
        Long id,
        String name,
        String registrationNumber,
        String email,
        String phone,
        BigDecimal reliabilityRate,
        LocalDateTime registrationDate) {
}
