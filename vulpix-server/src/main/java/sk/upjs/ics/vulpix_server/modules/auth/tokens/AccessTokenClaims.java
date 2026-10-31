package sk.upjs.ics.vulpix_server.modules.auth.tokens;

import java.util.UUID;

public record AccessTokenClaims(UUID userId, UUID sessionId, String username, String email) {}
