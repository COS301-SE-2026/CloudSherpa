package com.cloudsherpa.service.integration.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import com.cloudsherpa.lib.entities.User;
import com.cloudsherpa.lib.repositories.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {
  @Autowired UserRepository userRepository;

  @Container @ServiceConnection
  static PostgreSQLContainer timescaledb =
      new PostgreSQLContainer(
              DockerImageName.parse("timescale/timescaledb-ha:pg16-ts2.29")
                  .asCompatibleSubstituteFor("postgres"))
          .withCopyFileToContainer(
              MountableFile.forClasspathResource("sherpadb-schema.sql"),
              "/docker-entrypoint-initdb.d/01_schema.sql");

  @Test
  void testFindByEmailIgnoreCase() {
    User testUser = new User(UUID.randomUUID(), "test@gmail.com", "testuser", "test-password-hash");

    userRepository.save(testUser);

    User user = userRepository.findByEmailIgnoreCase("TEST@GMAIL.COM");

    assertThat(user).isNotNull();
    assertThat(user.getEmail()).isEqualTo("test@gmail.com");
  }
}
