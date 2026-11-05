package sk.upjs.ics.vulpix_server.modules.auth;

import sk.upjs.ics.dto.auth.SessionDto;

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
  public SessionDto toSessionDto() {
    return new SessionDto(this.id(), this.userId(), this.deviceInfo(), this.createdAt());
  }
}
