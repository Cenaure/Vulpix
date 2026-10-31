package sk.upjs.ics.daos;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import sk.upjs.ics.entities.UserEntity;

public interface UserDao {
  Optional<UserEntity> getById(UUID id);

  Optional<UserEntity> findByEmail(String email);

  List<UserEntity> searchByUsername(String query);

  UserEntity save(UserEntity user);

  void updateLastLogin(UUID userId, OffsetDateTime when);

  void deactivate(UUID userId);
}
