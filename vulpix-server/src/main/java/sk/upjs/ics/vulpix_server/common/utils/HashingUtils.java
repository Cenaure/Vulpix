package sk.upjs.ics.vulpix_server.common.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

// Component lets Spring inject this class into other classes' constructors
// This is a singleton
@Component
public final class HashingUtils {
  BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

  public String encode(String string) {
    return this.encoder.encode(string);
  }

  public boolean matches(String encoded, CharSequence raw) {
    return this.encoder.matches(raw, encoded);
  }
}
