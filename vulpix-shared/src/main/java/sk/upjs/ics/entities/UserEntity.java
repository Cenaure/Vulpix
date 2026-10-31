package sk.upjs.ics.entities;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserEntity(
    UUID id,
    String username,
    String email,
    UUID avatarImg,
    OffsetDateTime createdAt,
    OffsetDateTime deactivatedAt,
    OffsetDateTime lastLoginAt) {}
