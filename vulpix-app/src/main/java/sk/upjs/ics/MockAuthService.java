package sk.upjs.ics;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import sk.upjs.ics.auth.AuthErrorCode;
import sk.upjs.ics.auth.AuthException;
import sk.upjs.ics.auth.AuthResult;
import sk.upjs.ics.auth.AuthService;
import sk.upjs.ics.entities.UserEntity;

public class MockAuthService implements AuthService {

  private Map<String, String> passwords = new HashMap<>();
  private Map<String, UserEntity> users = new HashMap<>();

  public MockAuthService() {
    register("test", "test", "test");
  }

  @Override
  public AuthResult login(String email, String password) throws AuthException {
    email = email.toLowerCase();
    UserEntity user = users.get(email);
    if (user == null || !passwords.get(email).equals(password)) {
      throw new AuthException(AuthErrorCode.INVALID_CREDENTIALS);
    }
    var result = new AuthResult(user, UUID.randomUUID().toString(), UUID.randomUUID().toString());
    ;
    return result;
  }

  @Override
  public AuthResult register(String username, String email, String password) throws AuthException {
    username = username.toLowerCase();
    email = email.toLowerCase();
    if (users.containsKey(email)) throw new AuthException(AuthErrorCode.EMAIL_ALREADY_IN_USE);
    UserEntity user =
        new UserEntity(UUID.randomUUID(), username, email, null, OffsetDateTime.now(), null, null);
    users.put(email, user);
    passwords.put(email, password);
    return new AuthResult(user, UUID.randomUUID().toString(), UUID.randomUUID().toString());
  }

  @Override
  public void logout(String refreshToken) {}
}
