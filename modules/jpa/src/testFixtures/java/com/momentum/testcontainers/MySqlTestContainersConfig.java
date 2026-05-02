package com.momentum.testcontainers;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

@Configuration
@ConditionalOnProperty(name = "test.container.enabled", havingValue = "true")
public class MySqlTestContainersConfig {

  private static final MySQLContainer<?> mySqlContainer;

  static {
    mySqlContainer = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
        .withDatabaseName("momentum")
        .withUsername("test")
        .withPassword("test")
        .withExposedPorts(3306)
        .withCommand(
            "--character-set-server=utf8mb4",
            "--collation-server=utf8mb4_general_ci",
            "--skip-character-set-client-handshake"
        );
    System.setProperty("api.version", "1.44");

    mySqlContainer.start();

    String mySqlJdbcUrl = String.format(
        "jdbc:mysql://%s:%d/%s",
        mySqlContainer.getHost(),
        mySqlContainer.getFirstMappedPort(),
        mySqlContainer.getDatabaseName()
    );

    System.setProperty("datasource.mysql-jpa.main.jdbc-url", mySqlJdbcUrl);
    System.setProperty("datasource.mysql-jpa.main.username", mySqlContainer.getUsername());
    System.setProperty("datasource.mysql-jpa.main.password", mySqlContainer.getPassword());
    System.setProperty("datasource.mysql-jpa.main.driver-class-name", mySqlContainer.getDriverClassName());
  }
}
