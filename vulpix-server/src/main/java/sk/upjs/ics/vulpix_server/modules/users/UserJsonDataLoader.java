package sk.upjs.ics.vulpix_server.modules.users;

import java.io.IOException;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sk.upjs.ics.vulpix_server.modules.users.entities.UserSeed;
import sk.upjs.ics.vulpix_server.modules.users.entities.UserSeedList;
import tools.jackson.databind.ObjectMapper;

// https://www.youtube.com/watch?v=erp-7MCK5BU

// This component is used to make integrational tests of auth endpoints
@Component
@Slf4j // generates -> private static final org.slf4j.Logger log =
// org.slf4j.LoggerFactory.getLogger(UserJsonDataLoader.class);
@Profile({"dev", "e2e"}) // run only in one of these profiles / not in production
@RequiredArgsConstructor
public class UserJsonDataLoader implements CommandLineRunner {
  private final UserService userService;
  private final ObjectMapper objectMapper;

  @Value("classpath:data/users-seed.json")
  private Resource usersSeed;

  @Transactional // @Transactional means all or none
  @Override
  public void run(String... args) {
    if (this.userService.count() != 0) return;

    log.info("Loading users seed into the database");

    try (InputStream is = usersSeed.getInputStream()) {
      UserSeedList response = objectMapper.readValue(is, UserSeedList.class);

      for (UserSeed seed : response.users()) {
        this.userService.create(seed.username(), seed.email(), seed.password(), null);
      }

      log.info("Loaded {} users to the database", response.users().size());
    } catch (IOException e) {
      throw new RuntimeException("Failed to read JSON data", e);
    }
  }
}
