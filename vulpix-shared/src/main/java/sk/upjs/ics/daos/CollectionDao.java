package sk.upjs.ics.daos;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import sk.upjs.ics.entities.ApplicationEntity;
import sk.upjs.ics.entities.CollectionEntity;

public interface CollectionDao {
  List<CollectionEntity> getAllByUser(UUID userId);

  Optional<CollectionEntity> getById(UUID id);

  CollectionEntity save(CollectionEntity collection);

  void delete(UUID id);

  List<ApplicationEntity> getApplications(UUID collectionId);

  void addApplication(UUID collectionId, UUID applicationId);

  void removeApplication(UUID collectionId, UUID applicationId);
}
