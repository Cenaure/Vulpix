package sk.upjs.ics.entities;

import java.util.UUID;

public record ApplicationSpecificationsEntity(
    UUID applicationId,
    String applicationPath,
    String protonVersion,
    String winePrefix,
    String launchOptions) {}
