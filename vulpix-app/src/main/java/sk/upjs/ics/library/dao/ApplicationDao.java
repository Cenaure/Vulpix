package sk.upjs.ics.library.dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import sk.upjs.ics.entities.ApplicationEntity;

public interface ApplicationDao {
  List<ApplicationEntity> getAllByUser(UUID userId);

  Optional<ApplicationEntity> getById(UUID id);

  ApplicationEntity save(ApplicationEntity application);

  void delete(UUID id);
}
