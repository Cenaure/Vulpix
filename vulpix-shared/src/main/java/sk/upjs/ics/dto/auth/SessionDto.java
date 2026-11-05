package sk.upjs.ics.dto.auth;

import java.time.Instant;
import java.util.UUID;

public record SessionDto(
    UUID id,
    UUID userId,
    String deviceInfo,
    Instant createdAt
) { }
