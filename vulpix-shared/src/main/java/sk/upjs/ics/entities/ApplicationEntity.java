package sk.upjs.ics.entities;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ApplicationEntity(
    UUID id,
    UUID userId,
    String name,
    int playedMinutes,
    UUID backgroundImg,
    UUID logoImg,
    UUID iconImg,
    UUID coverImg,
    boolean isPrivate,
    OffsetDateTime addedAt) {}
