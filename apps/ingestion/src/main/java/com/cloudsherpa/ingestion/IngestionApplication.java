package com.cloudsherpa.ingestion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.cloudsherpa.lib.entities")
@EnableJpaRepositories(basePackages = "com.cloudsherpa.lib.repositories")
public class IngestionApplication {

  public static void main(String[] args) {
    SpringApplication.run(IngestionApplication.class, args);
  }
}
