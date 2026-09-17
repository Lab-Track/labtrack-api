package com.labtrack.labtrack.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record StudentCreateRequest(
        @NotBlank String name,
        @NotBlank String registrationNumber,
        @NotBlank @Email String email,
        String phone) {
}
