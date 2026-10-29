package sk.upjs.ics.vulpix_server.modules.auth;

import jakarta.validation.Valid;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import sk.upjs.ics.dto.auth.SignInDto;
import sk.upjs.ics.dto.auth.SignUpDto;
import sk.upjs.ics.dto.auth.TokenPair;
import sk.upjs.ics.vulpix_server.modules.users.User;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;

  @PostMapping("/sign-in")
  public TokenPair signIn(@Valid @RequestBody SignInDto dto) {
    return this.authService.signIn(dto.email(), dto.password(), dto.deviceInfo());
  }

  @PostMapping("/sign-up")
  public TokenPair signUp(@Valid @RequestBody SignUpDto dto) {
    return this.authService.signUp(dto.username(), dto.email(), dto.password(), dto.deviceInfo());
  }

  //
  //    @PostMapping("/logout")
  //    public void logout() {
  //        return;
  //    }
  //
  //    @PostMapping("/refresh")
  //    public TokenPair refresh() {
  //        return "todo";
  //    }
  //
  @GetMapping("/me")
  public User me(@AuthenticationPrincipal Jwt jwt) {
    UUID userId = UUID.fromString(jwt.getSubject());
    return this.authService.getMe(userId);
  }
}
