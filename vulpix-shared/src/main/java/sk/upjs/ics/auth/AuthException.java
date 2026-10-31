package sk.upjs.ics.auth;

public class AuthException extends RuntimeException {
  private final AuthErrorCode code;

  public AuthException(AuthErrorCode code) {
    super(code.name());
    this.code = code;
  }

  public AuthErrorCode getCode() {
    return code;
  }
}
