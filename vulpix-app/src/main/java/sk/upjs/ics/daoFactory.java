package sk.upjs.ics;

import sk.upjs.ics.collection.dao.CollectionDao;
import sk.upjs.ics.collection.dao.InMemoryCollectionDao;
import sk.upjs.ics.library.dao.ApplicationDao;
import sk.upjs.ics.library.dao.ApplicationSpecificationsDao;
import sk.upjs.ics.library.dao.InMemoryApplicationDao;
import sk.upjs.ics.library.dao.InMemoryApplicationSpecificationsDao;

public enum daoFactory {
  INSTANCE;

  private ApplicationDao applicationDao;
  private CollectionDao collectionDao;
  private ApplicationSpecificationsDao applicationSpecificationsDao;

  public ApplicationDao applicationDao() {
    if (applicationDao == null) {
      applicationDao = new InMemoryApplicationDao();
    }
    return applicationDao;
  }

  public CollectionDao collectionDao() {
    if (collectionDao == null) {
      collectionDao = new InMemoryCollectionDao(applicationDao());
    }
    return collectionDao;
  }

  public ApplicationSpecificationsDao applicationSpecificationsDao() {
    if (applicationSpecificationsDao == null) {
      applicationSpecificationsDao = new InMemoryApplicationSpecificationsDao();
    }
    return applicationSpecificationsDao;
  }
}
