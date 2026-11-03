package sk.upjs.ics.dto.auth;

import java.util.UUID;

public record UserProfileDto(
  UUID id,
  String username,
  String email,
  UUID avatarImg
) { }
