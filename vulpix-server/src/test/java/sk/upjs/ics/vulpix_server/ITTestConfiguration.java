package sk.upjs.ics.vulpix_server;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

// https://docs.spring.io/spring-boot/reference/testing/testcontainers.html
@TestConfiguration(proxyBeanMethods = false)
public class ITTestConfiguration {

  @Bean
  @ServiceConnection
  PostgreSQLContainer postgreSQLContainer() {
    return new PostgreSQLContainer(DockerImageName.parse("postgres:18-alpine"));
  }
}
