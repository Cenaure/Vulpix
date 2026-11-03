package sk.upjs.ics.vulpix_server.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;
import sk.upjs.ics.dto.auth.RefreshDto;
import sk.upjs.ics.dto.auth.SignInDto;
import sk.upjs.ics.dto.auth.SignUpDto;
import sk.upjs.ics.dto.auth.TokenPair;
import sk.upjs.ics.dto.auth.UserProfileDto;
import sk.upjs.ics.exceptions.ApiExceptionCodes;
import sk.upjs.ics.vulpix_server.ITTestConfiguration;
import sk.upjs.ics.vulpix_server.modules.auth.SessionService;
import sk.upjs.ics.vulpix_server.modules.users.UserJsonDataLoader;
import sk.upjs.ics.vulpix_server.modules.users.UserRepository;
import sk.upjs.ics.vulpix_server.modules.users.entities.User;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@ActiveProfiles("e2e")
@Import(ITTestConfiguration.class)
public class AuthIT {
  @Value("${jwt.access-ttl-minutes}")
  private long accessTTLMinutes;

  @Autowired private UserRepository userRepository;
  @Autowired private SessionService sessionService;

  @Autowired private RestTestClient client;

  @Autowired private JdbcClient jdbc;
  @Autowired private UserJsonDataLoader seedLoader;

  @BeforeEach
  void resetDatabase() {
    // CASCADE also clears sessions and other tables referencing users
    jdbc.sql("TRUNCATE TABLE users CASCADE").update();
    seedLoader.run();
  }

  @Test
  void seedUsersAreLoaded() {
    assertThat(userRepository.findByEmail("e2e-test1@test.test")).isPresent();
  }

  private TokenPair signIn() {
    return signIn("e2e");
  }

  private TokenPair signIn(String deviceInfo) {
    TokenPair tokens =
        this.client
            .post()
            .uri("/api/auth/sign-in")
            .contentType(MediaType.APPLICATION_JSON)
            .body(new SignInDto("e2e-test1@test.test", "e2e-01-1234", deviceInfo))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(TokenPair.class)
            .returnResult()
            .getResponseBody();

    assertThat(tokens).isNotNull();
    assertThat(tokens.accessToken()).isNotBlank();
    assertThat(tokens.refreshToken()).isNotBlank();

    return tokens;
  }

  @Test
  void signUpNewUser() {
    TokenPair tokens =
        client
            .post()
            .uri("/api/auth/sign-up")
            .contentType(MediaType.APPLICATION_JSON)
            .body(new SignUpDto("new-user", "new-user@test.test", "new-user-1234", "e2e"))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(TokenPair.class)
            .returnResult()
            .getResponseBody();

    assertThat(tokens).isNotNull();
    assertThat(tokens.accessToken()).isNotBlank();
    assertThat(tokens.refreshToken()).isNotBlank();
    client
        .get()
        .uri("/api/auth/me")
        .headers(headers -> headers.setBearerAuth(tokens.accessToken()))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.email")
        .isEqualTo("new-user@test.test")
        .jsonPath("$.username")
        .isEqualTo("new-user")
        .jsonPath("$.passwordHash")
        .doesNotHaveJsonPath();
  }

  @Test
  void signUpWithExistingUser() {
    client
        .post()
        .uri("/api/auth/sign-up")
        .contentType(MediaType.APPLICATION_JSON)
        .body(new SignUpDto("new-user", "e2e-test1@test.test", "new-user-1234", "e2e"))
        .exchange()
        .expectStatus()
        .is4xxClientError()
        .expectBody()
        .jsonPath("$.code")
        .isEqualTo(ApiExceptionCodes.EMAIL_TAKEN.name());

    client
        .post()
        .uri("/api/auth/sign-up")
        .contentType(MediaType.APPLICATION_JSON)
        .body(new SignUpDto("e2e-01", "new-user@test.test", "new-user-1234", "e2e"))
        .exchange()
        .expectStatus()
        .is4xxClientError()
        .expectBody()
        .jsonPath("$.code")
        .isEqualTo(ApiExceptionCodes.USERNAME_TAKEN.name());
  }

  @Test
  void signUpDtoValidation() {
    client
        .post()
        .uri("/api/auth/sign-up")
        .contentType(MediaType.APPLICATION_JSON)
        .body(new SignUpDto("", "new-user@test.test", "new-user-1234", "e2e"))
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  void signInWithSeedUser() {
    this.signIn();
  }

  @Test
  void signInDtoValidation() {
    this.client
        .post()
        .uri("/api/auth/sign-in")
        .contentType(MediaType.APPLICATION_JSON)
        .body(new SignInDto("", "e2e-01-1234", "e2e"))
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  void signInWithWrongCredentials() {
    this.client
        .post()
        .uri("/api/auth/sign-in")
        .contentType(MediaType.APPLICATION_JSON)
        .body(new SignInDto("e2e-test1@test.test", "wrong-password", "e2e"))
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody()
        .jsonPath("$.code")
        .isEqualTo(ApiExceptionCodes.WRONG_CREDENTIALS.name());
  }

  @Test
  void getMeReturnsUserData() {
    TokenPair tokens = this.signIn();

    UserProfileDto user =
        this.client
            .get()
            .uri("/api/auth/me")
            .header("Authorization", "Bearer %s".formatted(tokens.accessToken()))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(UserProfileDto.class)
            .returnResult()
            .getResponseBody();

    assertThat(user).isNotNull();
    assertThat(user.email()).isEqualTo("e2e-test1@test.test");
    assertThat(user.username()).isEqualTo("e2e-01");
    assertThat(user.avatarImg()).isNull();
  }

  @Test
  void getMeWithoutAuthHeader() {
    this.client.get().uri("/api/auth/me").exchange().expectStatus().isUnauthorized();
  }

  @Test
  void unauthorizedWhenTokenExpired() {
    User user = this.userRepository.findByEmail("e2e-test1@test.test").orElseThrow();

    // now - token_ttl (60 minutes) - 5 minutes
    // generated access token will be expired
    Instant expired =
        Instant.now().minus(accessTTLMinutes, ChronoUnit.MINUTES).minus(5, ChronoUnit.MINUTES);

    TokenPair tokens = this.sessionService.createSession(user, "e2e-test", expired);

    this.client
        .get()
        .uri("/api/auth/me")
        .header("Authorization", "Bearer %s".formatted(tokens.accessToken()))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void refreshTokens() {
    TokenPair oldTokens = this.signIn();

    TokenPair newTokens =
        this.client
            .post()
            .uri("/api/auth/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .body(new RefreshDto(oldTokens.refreshToken()))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(TokenPair.class)
            .returnResult()
            .getResponseBody();

    assertThat(newTokens).isNotNull();
    assertThat(newTokens.refreshToken()).isNotBlank().isNotEqualTo(oldTokens.refreshToken());
    assertThat(newTokens.accessToken()).isNotBlank().isNotEqualTo(oldTokens.accessToken());
    client
        .get()
        .uri("/api/auth/me")
        .headers(headers -> headers.setBearerAuth(newTokens.accessToken()))
        .exchange()
        .expectStatus()
        .isOk();
    refresh(newTokens.refreshToken());
  }

  @Test
  void oldRefreshTokenBecomeInvalid() {
    TokenPair oldTokens = this.signIn();

    // Refreshes tokens, old refresh token must become invalid
    this.client
        .post()
        .uri("/api/auth/refresh")
        .contentType(MediaType.APPLICATION_JSON)
        .body(new RefreshDto(oldTokens.refreshToken()))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(TokenPair.class);

    this.client
        .post()
        .uri("/api/auth/refresh")
        .contentType(MediaType.APPLICATION_JSON)
        .body(new RefreshDto(oldTokens.refreshToken()))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void logoutRevokesSession() {
    TokenPair tokens = this.signIn();
    TokenPair otherDevice = signIn("desktop");

    this.client
        .post()
        .uri("/api/auth/logout")
        .header("Authorization", "Bearer %s".formatted(tokens.accessToken()))
        .exchange()
        .expectStatus()
        .isNoContent();

    assertRefreshRejected(tokens.refreshToken());
    refresh(otherDevice.refreshToken());
  }

  @Test
  void revokesAllSessions() {
    TokenPair firstPair = signIn("android");
    TokenPair secondPair = signIn("desktop");

    this.client
        .post()
        .uri("/api/auth/revoke-all-sessions")
        .header("Authorization", "Bearer %s".formatted(secondPair.accessToken()))
        .exchange()
        .expectStatus()
        .isNoContent();

    assertRefreshRejected(firstPair.refreshToken());
    assertRefreshRejected(secondPair.refreshToken());
  }

  private TokenPair refresh(String refreshToken) {
    TokenPair tokens =
        client
            .post()
            .uri("/api/auth/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .body(new RefreshDto(refreshToken))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(TokenPair.class)
            .returnResult()
            .getResponseBody();
    assertThat(tokens).isNotNull();
    assertThat(tokens.accessToken()).isNotBlank();
    assertThat(tokens.refreshToken()).isNotBlank();
    return tokens;
  }

  private void assertRefreshRejected(String refreshToken) {
    client
        .post()
        .uri("/api/auth/refresh")
        .contentType(MediaType.APPLICATION_JSON)
        .body(new RefreshDto(refreshToken))
        .exchange()
        .expectStatus()
        .isUnauthorized()
        .expectBody()
        .jsonPath("$.code")
        .isEqualTo(ApiExceptionCodes.UNAUTHORIZED.name());
  }
}
