package sk.upjs.ics.daos;

import java.util.Optional;
import java.util.UUID;
import sk.upjs.ics.entities.ApplicationSpecificationsEntity;

public interface ApplicationSpecificationsDao {
  Optional<ApplicationSpecificationsEntity> getByApplicationId(UUID applicationId);

  ApplicationSpecificationsEntity save(ApplicationSpecificationsEntity specifications);

  void delete(UUID applicationId);
}
