package sk.upjs.ics.vulpix_server.modules.auth;

import java.time.Instant;
import java.util.UUID;

public record Session(
    UUID id,
    UUID userId,
    String refreshTokenSHA256,
    String deviceInfo,
    Instant createdAt,
    Instant expiresAt,
    Instant revokedAt) {
  public boolean isActive() {
    return expiresAt.isAfter(Instant.now()) && revokedAt == null;
  }
}
