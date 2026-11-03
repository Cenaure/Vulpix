package sk.upjs.ics.vulpix_server.modules.users.entities;

import java.time.Instant;
import java.util.UUID;

public record User(
    UUID id,
    String username,
    String email,
    String passwordHash,
    UUID avatarImg,
    Instant createdAt,
    Instant deactivatedAt,
    Instant lastLoginAt,
    Instant lastSeenAt) {}
