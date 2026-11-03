package sk.upjs.ics.collection.dao;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import sk.upjs.ics.entities.ApplicationEntity;
import sk.upjs.ics.entities.CollectionEntity;
import sk.upjs.ics.library.dao.ApplicationDao;

public class InMemoryCollectionDao implements CollectionDao {
  private final ApplicationDao applicationDao;

  private final Map<UUID, CollectionEntity> collections = new LinkedHashMap<>();
  private final Map<UUID, Set<UUID>> links = new HashMap<>();
  private final Set<UUID> seededUsers = new HashSet<>();

  public InMemoryCollectionDao(ApplicationDao applicationDao) {
    this.applicationDao = applicationDao;
  }

  @Override
  public synchronized List<CollectionEntity> getAllByUser(UUID userId) {
    seedIfNeeded(userId);
    return collections.values().stream()
        .filter(c -> c.userId().equals(userId))
        .sorted(Comparator.comparing(CollectionEntity::name, String.CASE_INSENSITIVE_ORDER))
        .toList();
  }

  @Override
  public synchronized Optional<CollectionEntity> getById(UUID id) {
    return Optional.ofNullable(collections.get(id));
  }

  @Override
  public synchronized CollectionEntity save(CollectionEntity collection) {
    UUID id = collection.id() != null ? collection.id() : UUID.randomUUID();
    CollectionEntity saved = new CollectionEntity(id, collection.userId(), collection.name());
    collections.put(id, saved);
    links.putIfAbsent(id, new LinkedHashSet<>());
    return saved;
  }

  @Override
  public synchronized void delete(UUID id) {
    collections.remove(id);
    links.remove(id);
  }

  @Override
  public synchronized List<ApplicationEntity> getApplications(UUID collectionId) {
    return links.getOrDefault(collectionId, Set.of()).stream()
        .map(applicationDao::getById)
        .flatMap(Optional::stream)
        .sorted(Comparator.comparing(ApplicationEntity::name, String.CASE_INSENSITIVE_ORDER))
        .toList();
  }

  @Override
  public synchronized void addApplication(UUID collectionId, UUID applicationId) {
    if (collections.containsKey(collectionId)) {
      links.computeIfAbsent(collectionId, k -> new LinkedHashSet<>()).add(applicationId);
    }
  }

  @Override
  public synchronized void removeApplication(UUID collectionId, UUID applicationId) {
    Set<UUID> apps = links.get(collectionId);
    if (apps != null) {
      apps.remove(applicationId);
    }
  }

  private void seedIfNeeded(UUID userId) {
    if (!seededUsers.add(userId)) {
      return;
    }
    List<ApplicationEntity> apps = applicationDao.getAllByUser(userId);

    CollectionEntity favorites = save(new CollectionEntity(null, userId, "Favorites"));
    CollectionEntity backlog = save(new CollectionEntity(null, userId, "Backlog"));

    for (String name : List.of("Hollow Knight", "Celeste", "Stardew Valley")) {
      addByName(apps, favorites, name);
    }
    for (String name : List.of("Dave the Diver", "Portal 2")) {
      addByName(apps, backlog, name);
    }
  }

  private void addByName(List<ApplicationEntity> apps, CollectionEntity collection, String name) {
    apps.stream()
        .filter(a -> a.name().equals(name))
        .findFirst()
        .ifPresent(a -> addApplication(collection.id(), a.id()));
  }
}
