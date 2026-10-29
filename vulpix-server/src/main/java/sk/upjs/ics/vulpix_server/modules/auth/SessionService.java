package sk.upjs.ics.vulpix_server.modules.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sk.upjs.ics.dto.auth.TokenPair;
import sk.upjs.ics.exceptions.ApiExceptionCodes;
import sk.upjs.ics.vulpix_server.common.HashingUtils;
import sk.upjs.ics.vulpix_server.common.exceptions.UnauthorizedException;
import sk.upjs.ics.vulpix_server.modules.auth.tokens.AccessTokenClaims;
import sk.upjs.ics.vulpix_server.modules.auth.tokens.RefreshTokenClaims;
import sk.upjs.ics.vulpix_server.modules.users.User;

// https://github.com/jwtk/jjwt#creating-a-jwt
@Service
@RequiredArgsConstructor
public class SessionService {
  @Value("${jwt.access-secret}")
  private String accessSecret;

  @Value("${jwt.refresh-secret}")
  private String refreshSecret;

  @Value("${jwt.access-ttl-minutes}")
  private long accessTTLMinutes;

  @Value("${jwt.refresh-ttl-days}")
  private long refreshTTLDays;

  private final HashingUtils hashingUtils;
  private final SessionRepository sessionRepository;

  public TokenPair generateAndSaveTokens(
      User user, boolean newSession, Optional<UUID> existingSessionId, String deviceInfo) {
    UUID sessionId = newSession ? UUID.randomUUID() : existingSessionId.orElse(null);
    if (sessionId == null) throw new UnauthorizedException(ApiExceptionCodes.UNAUTHORIZED);

    Instant now = Instant.now();

    SecretKey accessKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecret));

    String accessJwt =
        Jwts.builder()
            .issuer("vulpix")
            .subject(user.id().toString())
            .claim("sessionId", sessionId)
            .claim("username", user.username())
            .claim("email", user.email())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(accessTTLMinutes, ChronoUnit.MINUTES)))
            .signWith(accessKey, Jwts.SIG.HS256)
            .compact();

    SecretKey refreshKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshSecret));

    String refreshJwt =
        Jwts.builder()
            .issuer("vulpix")
            .subject(user.id().toString())
            .claim("sessionId", sessionId)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(refreshTTLDays, ChronoUnit.DAYS)))
            .signWith(refreshKey, Jwts.SIG.HS256)
            .compact();

    if (newSession)
      this.createSession(
          sessionId, user.id(), refreshJwt, deviceInfo, now.plus(refreshTTLDays, ChronoUnit.DAYS));
    else {
      this.updateSessionToken(sessionId, refreshJwt);
    }

    return new TokenPair(accessJwt, refreshJwt);
  }

  // region: # Validate tokens
  public RefreshTokenClaims validateRefreshToken(String refreshToken) throws JwtException {
    SecretKey refreshKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshSecret));

    Jwt<JwsHeader, Claims> res =
        Jwts.parser().verifyWith(refreshKey).build().parseSignedClaims(refreshToken);

    return new RefreshTokenClaims(
        UUID.fromString((String) res.getPayload().get("sessionId")),
        UUID.fromString(res.getPayload().getSubject()));
  }

  public AccessTokenClaims validateAccessToken(String accessToken) throws JwtException {
    SecretKey accessKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecret));

    Jwt<JwsHeader, Claims> res =
        Jwts.parser().verifyWith(accessKey).build().parseSignedClaims(accessToken);

    return new AccessTokenClaims(
        UUID.fromString((String) res.getPayload().get("sessionId")),
        UUID.fromString(res.getPayload().getSubject()),
        (String) res.getPayload().get("username"),
        (String) res.getPayload().get("email"));
  }

  // endregion: # Validate tokens

  // region: # Session Entity
  private Session createSession(
      UUID sessionId, UUID userId, String refreshToken, String deviceInfo, Instant expiresAt) {
    String refreshTokenHash = this.sha256(refreshToken);
    return this.sessionRepository.create(
        sessionId, userId, refreshTokenHash, deviceInfo, expiresAt);
  }

  private Session updateSessionToken(UUID sessionId, String refreshToken) {
    String refreshTokenHash = this.sha256(refreshToken);
    return this.sessionRepository
        .updateRefreshTokenHash(sessionId, refreshTokenHash)
        .orElseThrow(() -> new UnauthorizedException(ApiExceptionCodes.UNAUTHORIZED));
  }

  public Optional<Session> findByRefreshTokenHash(String hash) {
    return this.sessionRepository.findByRefreshTokenHash(hash);
  }

  public Number deleteSession(UUID id) {
    return this.sessionRepository.delete(id);
  }

  // endregion: # Session Entity

  public String sha256(String token) {
    try {
      byte[] hash =
          MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));

      return HexFormat.of().formatHex(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 unavailable", e);
    }
  }
}
