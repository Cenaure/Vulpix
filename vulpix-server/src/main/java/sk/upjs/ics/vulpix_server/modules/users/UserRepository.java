package sk.upjs.ics.vulpix_server.modules.users;

import static sk.upjs.ics.vulpix_server.common.utils.MapperHelpers.mapToInstant;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.container.ContainerImageMetadata;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserRepository {
  private final JdbcClient jdbc;

  private static final RowMapper<User> mapper =
      (rs, rn) ->
          new User(
              rs.getObject("id", UUID.class),
              rs.getObject("username", String.class),
              rs.getObject("email", String.class),
              rs.getObject("password_hash", String.class),
              rs.getObject("avatar_img", UUID.class),
              mapToInstant(rs, "created_at"),
              mapToInstant(rs, "deactivated_at"),
              mapToInstant(rs, "last_login_at"),
              mapToInstant(rs, "last_seen_at"));

  public User create(String username, String email, String passwordHash, UUID avatarImg) {
    return jdbc.sql(
            """
            INSERT INTO users(username, email, password_hash, avatar_img)
            VALUES (:username, :email, :password_hash, :avatar_img)
            RETURNING id, username, email, password_hash, avatar_img, created_at, deactivated_at, last_login_at, last_seen_at
            """)
        .param("username", username)
        .param("email", email)
        .param("password_hash", passwordHash)
        .param("avatar_img", avatarImg)
        .query(mapper)
        .single();
  }

  public Optional<User> findById(UUID id) {
    return jdbc.sql(
            """
            SELECT id, username, email, password_hash, avatar_img, created_at, deactivated_at, last_login_at, last_seen_at
            FROM users
            WHERE id = :id AND deactivated_at IS NULL
            """)
        .param("id", id)
        .query(mapper)
        .optional();
  }

  public Optional<User> findByEmail(String email) {
    return jdbc.sql(
            """
            SELECT id, username, email, password_hash, avatar_img, created_at, deactivated_at, last_login_at, last_seen_at
            FROM users
            WHERE email = :email AND deactivated_at IS NULL
            """)
        .param("email", email)
        .query(mapper)
        .optional();
  }

  public Optional<User>  findByUsername(String username) {
    return jdbc.sql(
            """
            SELECT id, username, email, password_hash, avatar_img, created_at, deactivated_at, last_login_at, last_seen_at
            FROM users
            WHERE username = :username AND deactivated_at IS NULL
            """)
        .param("username", username)
        .query(mapper)
        .optional();
  }

  public Optional<User> deactivate(UUID id) {
    return jdbc.sql(
            """
            UPDATE users
            SET deactivated_at = now()
            WHERE id = :id AND deactivated_at IS NULL
            RETURNING id, username, email, password_hash, avatar_img, created_at, deactivated_at, last_login_at, last_seen_at
            """)
        .param("id", id)
        .query(mapper)
        .optional();
  }

  public void updateLastLoginDate(UUID id) {
    jdbc.sql(
            """
            UPDATE users
            SET last_login_at = now()
            WHERE id = :id AND deactivated_at IS NULL
            """)
        .param("id", id)
        .update();
  }

  public Optional<User> updateAvatar(UUID id, UUID newAvatarUUID) {
    return jdbc.sql(
            """
            UPDATE users
            SET avatar_img = :avatar_url
            WHERE id = :id AND deactivated_at IS NULL
            RETURNING id, username, email, password_hash, avatar_img, created_at, deactivated_at, last_login_at, last_seen_at
            """)
        .param("id", id)
        .param("avatar_url", newAvatarUUID)
        .query(mapper)
        .optional();
  }
}
