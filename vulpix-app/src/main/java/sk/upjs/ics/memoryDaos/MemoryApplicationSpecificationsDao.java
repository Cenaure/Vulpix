package sk.upjs.ics.memoryDaos;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import sk.upjs.ics.daos.ApplicationSpecificationsDao;
import sk.upjs.ics.entities.ApplicationSpecificationsEntity;

public class MemoryApplicationSpecificationsDao implements ApplicationSpecificationsDao {
  private final Map<UUID, ApplicationSpecificationsEntity> store = new HashMap<>();

  @Override
  public synchronized Optional<ApplicationSpecificationsEntity> getByApplicationId(
      UUID applicationId) {
    return Optional.ofNullable(store.get(applicationId));
  }

  @Override
  public synchronized ApplicationSpecificationsEntity save(ApplicationSpecificationsEntity spec) {
    store.put(spec.applicationId(), spec);
    return spec;
  }

  @Override
  public synchronized void delete(UUID applicationId) {
    store.remove(applicationId);
  }
}
