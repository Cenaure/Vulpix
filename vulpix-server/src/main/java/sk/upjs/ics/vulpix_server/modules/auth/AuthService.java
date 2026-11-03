package sk.upjs.ics.vulpix_server.modules.auth;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sk.upjs.ics.dto.auth.TokenPair;
import sk.upjs.ics.exceptions.ApiExceptionCodes;
import sk.upjs.ics.vulpix_server.common.exceptions.BadRequestException;
import sk.upjs.ics.vulpix_server.common.exceptions.ForbiddenException;
import sk.upjs.ics.vulpix_server.common.exceptions.NotFoundException;
import sk.upjs.ics.vulpix_server.common.exceptions.UnauthorizedException;
import sk.upjs.ics.vulpix_server.common.utils.PasswordHashing;
import sk.upjs.ics.vulpix_server.modules.auth.tokens.RefreshTokenClaims;
import sk.upjs.ics.vulpix_server.modules.users.UserService;
import sk.upjs.ics.vulpix_server.modules.users.entities.User;

@Service
@RequiredArgsConstructor
public class AuthService {
  private final UserService userService;
  private final PasswordHashing passwordHashing;
  private final SessionService sessionService;

  @Value("${auth.fake-password}")
  private String fakePassword;

  public TokenPair signUp(String username, String email, String password, String deviceInfo) {
    User user = this.userService.create(username, email, password, null);

    return this.sessionService.createSession(user, deviceInfo, Instant.now());
  }

  public TokenPair signIn(String email, String password, String deviceInfo) {
    Optional<User> existingUser = this.userService.findByEmail(email);

    String hashToCompare =
        existingUser.isPresent() ? existingUser.get().passwordHash() : fakePassword;

    boolean passwordMatches = passwordHashing.matches(hashToCompare, password);

    if (existingUser.isEmpty() || !passwordMatches)
      throw new BadRequestException(ApiExceptionCodes.WRONG_CREDENTIALS);

    User unwrappedUser = existingUser.get();

    this.userService.updateLastLoginDate(unwrappedUser.id());

    return this.sessionService.createSession(unwrappedUser, deviceInfo, Instant.now());
  }

  public TokenPair refresh(String refreshToken) {
    try {
      RefreshTokenClaims claims = this.sessionService.validateRefreshToken(refreshToken);

      User user = this.userService.findById(claims.userId());

      this.userService.updateLastLoginDate(user.id());

      return this.sessionService.updateSessionToken(user, claims.sessionId(), Instant.now());
    } catch (Exception e) {
      throw new UnauthorizedException(ApiExceptionCodes.UNAUTHORIZED);
    }
  }

  public Session logout(UUID sessionId, UUID userId) {
    Session session =
        this.sessionService
            .findBySessionId(sessionId)
            .orElseThrow(() -> new UnauthorizedException(ApiExceptionCodes.UNAUTHORIZED));

    if (!session.userId().equals(userId)) throw new ForbiddenException(ApiExceptionCodes.FORBIDDEN);

    return this.sessionService.revokeSession(sessionId);
  }

  public void revokeAllUserSessions(UUID userId) {
    this.sessionService.revokeAllSessions(userId);
  }

  public User getMe(UUID id) {
    try {
      return this.userService.findById(id);
    } catch (NotFoundException e) {
      throw new UnauthorizedException(ApiExceptionCodes.UNAUTHORIZED);
    }
  }
}
