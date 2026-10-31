package sk.upjs.ics.daos;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import sk.upjs.ics.entities.PlaySessionEntity;

public interface PlaySessionDao {
  PlaySessionEntity start(UUID applicationId);

  void finish(UUID playSessionId);

  Optional<PlaySessionEntity> getById(UUID id);

  List<PlaySessionEntity> getByApplication(UUID applicationId);

  List<PlaySessionEntity> getRecentByUser(UUID userId, int limit);

  void setCurrentPlaySession(UUID userId, UUID playSessionIdOrNull);
}
