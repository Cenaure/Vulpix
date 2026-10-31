package sk.upjs.ics.entities;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MessageEntity(
    UUID id, UUID chatId, UUID authorId, String content, OffsetDateTime sentAt) {}
