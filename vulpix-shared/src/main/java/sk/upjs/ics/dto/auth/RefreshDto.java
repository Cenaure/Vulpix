package sk.upjs.ics.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshDto(@NotBlank String refreshToken) {}
