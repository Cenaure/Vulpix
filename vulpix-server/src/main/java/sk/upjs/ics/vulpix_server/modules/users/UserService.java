package sk.upjs.ics.vulpix_server.modules.users;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sk.upjs.ics.exceptions.ApiExceptionCodes;
import sk.upjs.ics.vulpix_server.common.exceptions.ConflictException;
import sk.upjs.ics.vulpix_server.common.exceptions.NotFoundException;
import sk.upjs.ics.vulpix_server.common.utils.PasswordHashing;
import sk.upjs.ics.vulpix_server.modules.users.entities.User;
import sk.upjs.ics.vulpix_server.modules.users.entities.UserSeed;

@Service
@RequiredArgsConstructor
public class UserService {
  private final PasswordHashing passwordHashing;
  private final UserRepository userRepository;

  public User create(String username, String email, String password, UUID avatarImg) {
    boolean emailTaken = this.userRepository.findByEmail(email).isPresent();
    if (emailTaken) throw new ConflictException(ApiExceptionCodes.EMAIL_TAKEN);

    boolean usernameTaken = this.userRepository.findByUsername(username).isPresent();
    if (usernameTaken) throw new ConflictException(ApiExceptionCodes.USERNAME_TAKEN);

    String passwordHash = this.passwordHashing.encode(password);
    return this.userRepository.create(username, email, passwordHash, avatarImg);
  }

  public void bulkCreate(List<UserSeed> users) {}

  public User findById(UUID id) {
    return this.userRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException(ApiExceptionCodes.USER_NOT_FOUND));
  }

  public Optional<User> findByEmail(String email) {
    return this.userRepository.findByEmail(email);
  }

  public User deactivate(UUID id) {
    return this.userRepository
        .deactivate(id)
        .orElseThrow(() -> new NotFoundException(ApiExceptionCodes.USER_NOT_FOUND));
  }

  public void updateLastLoginDate(UUID id) {
    this.userRepository.updateLastLoginDate(id);
  }

  public Integer count() {
    return this.userRepository.count();
  }
}
