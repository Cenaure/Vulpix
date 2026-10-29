package sk.upjs.ics.vulpix_server.common;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;

public final class MapperHelpers {
  public static Instant mapToInstant(ResultSet resultSet, String field) throws SQLException {
    var value = resultSet.getObject(field, OffsetDateTime.class);
    return value == null ? null : value.toInstant();
  }
}
