package sk.upjs.ics.auth;

public interface AuthService {
  AuthResult login(String email, String password) throws AuthException;

  AuthResult register(String username, String email, String password) throws AuthException;

  void logout(String refreshToken);
}
