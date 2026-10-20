package sk.upjs.ics.vulpix_server.modules.auth;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SessionRepository {
  private final JdbcClient jdbc;

  private static Instant mapToInstant(ResultSet resultSet, String field) throws SQLException {
    var value = resultSet.getObject(field, OffsetDateTime.class);
    return value == null ? null : value.toInstant();
  }

  private static final RowMapper<Session> mapper =
      (resultSet, rowNum) ->
          new Session(
              resultSet.getObject("id", UUID.class),
              resultSet.getObject("user_id", UUID.class),
              resultSet.getObject("refresh_token_hash", String.class),
              resultSet.getObject("device_info", String.class),
              mapToInstant(resultSet, "created_at"),
              mapToInstant(resultSet, "expires_at"),
              mapToInstant(resultSet, "revoked_at"));

  public Session create(
      UUID userId, String refreshTokenHash, String deviceInfo, Instant expiresAt) {
    return jdbc.sql(
            """
            INSERT INTO sessions(user_id, device_info, refresh_token_hash, expires_at)
            VALUES (:user_id, :device_info, :refresh_token_hash, :expires_at)
            RETURNING id, user_id, refresh_token_hash, device_info, created_at, expires_at, revoked_at
            """)
        .param("user_id", userId)
        .param("device_info", deviceInfo)
        .param("refresh_token_hash", refreshTokenHash)
        .param("expires_at", expiresAt)
        .query(mapper)
        .single();
  }

  public List<Session> findByUserId(UUID userId) {
    return jdbc.sql(
            """
            SELECT id, user_id, device_info, refresh_token_hash, created_at, expires_at, revoked_at
            FROM sessions
            WHERE user_id = :user_id
            """)
        .param("user_id", userId)
        .query(mapper)
        .list();
  }

  public Optional<Session> findByRefreshTokenHash(String refreshTokenHash) {
    return jdbc.sql(
            """
            SELECT id, user_id, device_info, refresh_token_hash, created_at, expires_at, revoked_at
            FROM sessions
            WHERE refresh_token_hash = :refresh_token_hash
            """)
        .param("refresh_token_hash", refreshTokenHash)
        .query(mapper)
        .optional();
  }

  public Optional<Session> revokeSession(UUID id) {
    return jdbc.sql(
            """
            UPDATE sessions
            SET revoked_at = now()
            WHERE id = :id
            RETURNING id, user_id, device_info, refresh_token_hash, created_at, expires_at, revoked_at
            """)
        .param("id", id)
        .query(mapper)
        .optional();
  }
}
