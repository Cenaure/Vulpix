package sk.upjs.ics.vulpix_server;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("e2e")
@Import(ITTestConfiguration.class)
class VulpixServerApplicationTests {

  @Test
  void contextLoads() {}
}
