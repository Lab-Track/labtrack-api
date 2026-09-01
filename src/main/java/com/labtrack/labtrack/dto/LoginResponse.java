package com.labtrack.labtrack.dto;

import java.time.Instant;

public record LoginResponse(String token, Instant expiresAt) {
}
