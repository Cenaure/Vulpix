package sk.upjs.ics.vulpix_server.modules.auth.tokens;

import java.util.UUID;

public record RefreshTokenClaims(UUID userId, UUID sessionId) {}
