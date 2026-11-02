package sk.upjs.ics;

import sk.upjs.ics.daos.ApplicationDao;
import sk.upjs.ics.daos.ApplicationSpecificationsDao;
import sk.upjs.ics.daos.CollectionDao;
import sk.upjs.ics.memoryDaos.MemoryApplicationDao;
import sk.upjs.ics.memoryDaos.MemoryApplicationSpecificationsDao;
import sk.upjs.ics.memoryDaos.MemoryCollectionDao;

public enum daoFactory {
  INSTANCE;

  private ApplicationDao applicationDao;
  private CollectionDao collectionDao;
  private ApplicationSpecificationsDao applicationSpecificationsDao;

  public ApplicationDao applicationDao() {
    if (applicationDao == null) {
      applicationDao = new MemoryApplicationDao();
    }
    return applicationDao;
  }

  public CollectionDao collectionDao() {
    if (collectionDao == null) {
      collectionDao = new MemoryCollectionDao(applicationDao());
    }
    return collectionDao;
  }

  public ApplicationSpecificationsDao applicationSpecificationsDao() {
    if (applicationSpecificationsDao == null) {
      applicationSpecificationsDao = new MemoryApplicationSpecificationsDao();
    }
    return applicationSpecificationsDao;
  }
}
