package sk.upjs.ics.entities;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ChatEntity(
    UUID id, UUID userIdFrom, UUID userIdTo, int unreadMessages, OffsetDateTime createdAt) {}
