package sk.upjs.ics.vulpix_server.modules.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sk.upjs.ics.dto.auth.SignInDto;
import sk.upjs.ics.dto.auth.SignUpDto;
import sk.upjs.ics.dto.auth.TokenPair;

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
  //    @GetMapping("/me")
  //    public String me() {
  //        return "todo";
  //    }

}
