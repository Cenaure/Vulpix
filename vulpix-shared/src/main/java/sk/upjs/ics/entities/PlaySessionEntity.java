package sk.upjs.ics.entities;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PlaySessionEntity(
    UUID id, UUID applicationId, OffsetDateTime startedAt, OffsetDateTime finishedAt) {}
