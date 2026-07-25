package com.cloudsherpa.service.integration.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import com.cloudsherpa.lib.entities.User;
import com.cloudsherpa.lib.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {
  @Autowired UserRepository userRepository;

  @Container @ServiceConnection
  static PostgreSQLContainer timescaledb =
      new PostgreSQLContainer(
              DockerImageName.parse("timescale/timescaledb:2.16.1-pg16")
                  .asCompatibleSubstituteFor("postgres"))
          .withInitScript("sherpadb-schema.sql");

  @Test
  void testFindByEmailIgnoreCase() {
    User user = userRepository.findByEmailIgnoreCase("demo@gmail.com");
    assertThat(user).isNotNull();
  }
}
