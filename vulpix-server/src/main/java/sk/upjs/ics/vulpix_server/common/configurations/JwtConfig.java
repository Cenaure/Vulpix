package sk.upjs.ics.vulpix_server.common.configurations;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
public class JwtConfig {
  @Value("${jwt.issuer}")
  private String jwtIssuer;

  private final SecretKey accessKey;

  public JwtConfig(@Value("${jwt.access-secret}") String accessSecret) {
    this.accessKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecret));
  }

  @Bean
  public JwtDecoder jwtDecoder() {
    NimbusJwtDecoder decoder =
        NimbusJwtDecoder.withSecretKey(accessKey).macAlgorithm(MacAlgorithm.HS256).build();

    decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(jwtIssuer));

    return decoder;
  }
}
