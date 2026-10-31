package sk.upjs.ics.entities;

import java.time.OffsetDateTime;
import java.util.UUID;

public record FriendshipEntity(
    UUID userIdFrom, UUID userIdTo, OffsetDateTime createdAt, OffsetDateTime acceptedAt) {}
