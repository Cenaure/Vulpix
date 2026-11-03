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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sk.upjs.ics.dto.auth.TokenPair;
import sk.upjs.ics.exceptions.ApiExceptionCodes;
import sk.upjs.ics.vulpix_server.common.exceptions.UnauthorizedException;
import sk.upjs.ics.vulpix_server.modules.auth.tokens.RefreshTokenClaims;
import sk.upjs.ics.vulpix_server.modules.users.entities.User;

// https://github.com/jwtk/jjwt#creating-a-jwt
@Service
public class SessionService {
  @Value("${jwt.access-ttl-minutes}")
  private long accessTTLMinutes;

  @Value("${jwt.refresh-ttl-days}")
  private long refreshTTLDays;

  @Value("${jwt.issuer}")
  private String jwtIssuer;

  private final SessionRepository sessionRepository;

  private final SecretKey accessKey;
  private final SecretKey refreshKey;

  public SessionService(
      SessionRepository sessionRepository,
      @Value("${jwt.access-secret}") String accessSecret,
      @Value("${jwt.refresh-secret}") String refreshSecret) {
    this.sessionRepository = sessionRepository;
    this.accessKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecret));
    this.refreshKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshSecret));
  }

  private TokenPair generateTokens(User user, UUID sessionId, Instant now) {
    String accessJwt =
        Jwts.builder()
            .id(UUID.randomUUID().toString()) // Makes tokens generated in the same second different
            .issuer(jwtIssuer)
            .subject(user.id().toString())
            .claim("sessionId", sessionId.toString())
            .claim("username", user.username())
            .claim("email", user.email())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(accessTTLMinutes, ChronoUnit.MINUTES)))
            .signWith(accessKey, Jwts.SIG.HS256)
            .compact();

    String refreshJwt =
        Jwts.builder()
            .id(UUID.randomUUID().toString())
            .issuer(jwtIssuer)
            .subject(user.id().toString())
            .claim("sessionId", sessionId.toString())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(refreshTTLDays, ChronoUnit.DAYS)))
            .signWith(refreshKey, Jwts.SIG.HS256)
            .compact();

    return new TokenPair(accessJwt, refreshJwt);
  }

  // region: # Validate tokens
  public RefreshTokenClaims validateRefreshToken(String refreshToken) throws JwtException {
    Jwt<JwsHeader, Claims> res =
        Jwts.parser()
            .verifyWith(refreshKey)
            .requireIssuer(jwtIssuer)
            .build()
            .parseSignedClaims(refreshToken);

    RefreshTokenClaims claims = new RefreshTokenClaims(
        UUID.fromString(res.getPayload().getSubject()),
        UUID.fromString((String) res.getPayload().get("sessionId")));

    // Checks whether the session exists and is not revoked
    String refreshTokenHash = this.sha256(refreshToken);
    this.findByRefreshTokenHash(refreshTokenHash)
        .orElseThrow(() -> new UnauthorizedException(ApiExceptionCodes.UNAUTHORIZED));

    return claims;
  }

  //  public AccessTokenClaims validateAccessToken(String accessToken) throws JwtException {
  //    Jwt<JwsHeader, Claims> res =
  //        Jwts.parser()
  //            .verifyWith(accessKey)
  //            .requireIssuer(jwtIssuer)
  //            .build()
  //            .parseSignedClaims(accessToken);
  //
  //    return new AccessTokenClaims(
  //        UUID.fromString((String) res.getPayload().get("sessionId")),
  //        UUID.fromString(res.getPayload().getSubject()),
  //        (String) res.getPayload().get("username"),
  //        (String) res.getPayload().get("email"));
  //  }

  // endregion: # Validate tokens

  // region: # Session Entity
  public TokenPair createSession(User user, String deviceInfo, Instant now) {
    UUID sessionId = UUID.randomUUID();

    TokenPair tokens = generateTokens(user, sessionId, now);

    Instant expiresAt = now.plus(refreshTTLDays, ChronoUnit.DAYS);

    String refreshTokenHash = this.sha256(tokens.refreshToken());
    this.sessionRepository.create(sessionId, user.id(), refreshTokenHash, deviceInfo, expiresAt);

    return tokens;
  }

  public TokenPair updateSessionToken(User user, UUID sessionId, Instant now) {
    TokenPair tokens = generateTokens(user, sessionId, now);

    String refreshTokenHash = this.sha256(tokens.refreshToken());
    this.sessionRepository
        .updateRefreshTokenHash(sessionId, refreshTokenHash)
        .orElseThrow(() -> new UnauthorizedException(ApiExceptionCodes.UNAUTHORIZED));

    return tokens;
  }

  public Optional<Session> findByRefreshTokenHash(String hash) {
    return this.sessionRepository.findByRefreshTokenHash(hash);
  }

  public Optional<Session> findBySessionId(UUID sessionId) {
    return this.sessionRepository.findBySessionId(sessionId);
  }

  public Session revokeSession(UUID id) {
    return this.sessionRepository
        .revoke(id)
        .orElseThrow(() -> new UnauthorizedException(ApiExceptionCodes.UNAUTHORIZED));
  }

  public void revokeAllSessions(UUID userId) {
    this.sessionRepository.revokeAllUserSessions(userId);
  }

  // endregion: # Session Entity

  // TODO move to idk static final sealed public protected ultra galaxy duo fold class
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
