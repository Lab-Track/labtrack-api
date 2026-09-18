package com.labtrack.labtrack.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record TechnicianCreateRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank String password) {
}
