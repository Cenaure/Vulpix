package sk.upjs.ics.vulpix_server.modules.auth;

import static sk.upjs.ics.vulpix_server.common.utils.MapperHelpers.mapToInstant;

import java.time.Instant;
import java.time.ZoneOffset;
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

  private static final RowMapper<Session> mapper =
      (rs, rn) ->
          new Session(
              rs.getObject("id", UUID.class),
              rs.getObject("user_id", UUID.class),
              rs.getObject("refresh_token_hash", String.class),
              rs.getObject("device_info", String.class),
              mapToInstant(rs, "created_at"),
              mapToInstant(rs, "expires_at"),
              mapToInstant(rs, "revoked_at"));

  public Optional<Session> findBySessionId(UUID sessionId) {
    return jdbc.sql(
            """
            SELECT id, user_id, device_info, refresh_token_hash, created_at, expires_at, revoked_at
            FROM sessions
            WHERE id = :id AND revoked_at IS NULL AND expires_at > now()
            """)
        .param("id", sessionId)
        .query(mapper)
        .optional();
  }

  public Session create(
      UUID sessionId, UUID userId, String refreshTokenHash, String deviceInfo, Instant expiresAt) {
    return jdbc.sql(
            """
            INSERT INTO sessions(id, user_id, device_info, refresh_token_hash, expires_at)
            VALUES (:session_id, :user_id, :device_info, :refresh_token_hash, :expires_at)
            RETURNING id, user_id, refresh_token_hash, device_info, created_at, expires_at, revoked_at
            """)
        .param("session_id", sessionId)
        .param("user_id", userId)
        .param("device_info", deviceInfo)
        .param("refresh_token_hash", refreshTokenHash)
        .param("expires_at", expiresAt.atOffset(ZoneOffset.UTC))
        .query(mapper)
        .single();
  }

  public Optional<Session> updateRefreshTokenHash(UUID id, String newHash) {
    return jdbc.sql(
            """
            UPDATE sessions
            SET refresh_token_hash = :refresh_token_hash
            WHERE id = :id AND revoked_at IS NULL AND expires_at > now()
            RETURNING id, user_id, device_info, refresh_token_hash, created_at, expires_at, revoked_at
            """)
        .param("id", id)
        .param("refresh_token_hash", newHash)
        .query(mapper)
        .optional();
  }

  public List<Session> findByUserId(UUID userId) {
    return jdbc.sql(
            """
            SELECT id, user_id, device_info, refresh_token_hash, created_at, expires_at, revoked_at
            FROM sessions
            WHERE user_id = :user_id AND revoked_at IS NULL AND expires_at > now()
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
            WHERE refresh_token_hash = :refresh_token_hash AND revoked_at IS NULL AND expires_at > now()
            """)
        .param("refresh_token_hash", refreshTokenHash)
        .query(mapper)
        .optional();
  }

  public Optional<Session> revoke(UUID id) {
    return jdbc.sql(
            """
            UPDATE sessions
            SET revoked_at = now()
            WHERE id = :id AND revoked_at IS NULL AND expires_at > now()
            RETURNING id, user_id, device_info, refresh_token_hash, created_at, expires_at, revoked_at
            """)
        .param("id", id)
        .query(mapper)
        .optional();
  }

  public void revokeAllUserSessions(UUID userId) {
    jdbc.sql(
            """
            UPDATE sessions
            SET revoked_at = now()
            WHERE user_id = :user_id AND revoked_at IS NULL
            """)
        .param("user_id", userId)
        .update();
  }

  // TODO schedule deleting every day ??
  public Number deleteExpired() {
    return jdbc.sql(
            """
            WITH deleted AS (
                DELETE FROM sessions WHERE expires_at < now() RETURNING *
            ) SELECT count(*) FROM deleted
            """)
        .query(Number.class)
        .single();
  }
}
