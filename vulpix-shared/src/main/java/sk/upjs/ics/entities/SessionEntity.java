package sk.upjs.ics.entities;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SessionEntity(
    UUID id,
    UUID userId,
    String refreshToken,
    String deviceInfo,
    OffsetDateTime createdAt,
    OffsetDateTime expiresAt,
    OffsetDateTime revokedAt) {}
