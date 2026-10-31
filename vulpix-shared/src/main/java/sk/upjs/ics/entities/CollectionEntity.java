package sk.upjs.ics.entities;

import java.util.UUID;

public record CollectionEntity(UUID id, UUID userId, String name) {
  @Override
  public String toString() {
    return name;
  }
}
