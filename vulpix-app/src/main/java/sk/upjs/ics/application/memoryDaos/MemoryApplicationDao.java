package sk.upjs.ics.application.memoryDaos;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import sk.upjs.ics.daos.ApplicationDao;
import sk.upjs.ics.entities.ApplicationEntity;

public class MemoryApplicationDao implements ApplicationDao {
  private static final String[] DEMO_NAMES = {
    "Hollow Knight", "Celeste", "Stardew Valley", "Portal 2", "Terraria", "Dave the Diver"
  };
  private static final int[] DEMO_MINUTES = {1520, 340, 4200, 610, 2750, 0};

  private final Map<UUID, ApplicationEntity> store = new LinkedHashMap<>();
  private final Set<UUID> seededUsers = new HashSet<>();

  @Override
  public synchronized List<ApplicationEntity> getAllByUser(UUID userId) {
    seedIfNeeded(userId);
    return store.values().stream()
        .filter(app -> app.userId().equals(userId))
        .sorted(Comparator.comparing(ApplicationEntity::name, String.CASE_INSENSITIVE_ORDER))
        .toList();
  }

  @Override
  public synchronized Optional<ApplicationEntity> getById(UUID id) {
    return Optional.ofNullable(store.get(id));
  }

  @Override
  public synchronized ApplicationEntity save(ApplicationEntity app) {
    UUID id = app.id() != null ? app.id() : UUID.randomUUID();
    OffsetDateTime addedAt = app.addedAt() != null ? app.addedAt() : OffsetDateTime.now();
    ApplicationEntity saved =
        new ApplicationEntity(
            id,
            app.userId(),
            app.name(),
            app.playedMinutes(),
            app.backgroundImg(),
            app.logoImg(),
            app.iconImg(),
            app.coverImg(),
            app.isPrivate(),
            addedAt);
    store.put(id, saved);
    return saved;
  }

  @Override
  public synchronized void delete(UUID id) {
    store.remove(id);
  }

  private void seedIfNeeded(UUID userId) {
    if (!seededUsers.add(userId)) {
      return; // tohto používateľa sme už naplnili
    }
    for (int i = 0; i < DEMO_NAMES.length; i++) {
      save(
          new ApplicationEntity(
              null, userId, DEMO_NAMES[i], DEMO_MINUTES[i], null, null, null, null, false, null));
    }
  }
}
