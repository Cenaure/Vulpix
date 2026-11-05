package sk.upjs.ics.dto.auth;

import java.util.List;

public record GetUserSessionsDto(
    List<SessionDto> sessions
) { }
