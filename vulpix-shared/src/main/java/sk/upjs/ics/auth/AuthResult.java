package sk.upjs.ics.auth;

import sk.upjs.ics.entities.UserEntity;

public record AuthResult(UserEntity user, String accessToken, String refreshToken) {}
