package sk.upjs.ics.daos;

import java.util.Optional;
import sk.upjs.ics.entities.SessionEntity;

public interface SessionDao {
  SessionEntity save(SessionEntity session);

  Optional<SessionEntity> findByRefreshToken(String refreshToken);

  int deleteExpired();
}
