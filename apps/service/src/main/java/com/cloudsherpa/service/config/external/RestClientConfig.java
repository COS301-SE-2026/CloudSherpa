package com.cloudsherpa.service.config.external;

import java.net.http.HttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
  @Bean
  RestClient intelligenceRestClient(
      RestClient.Builder builder, @Value("${intelligence-url}") String intelligenceUrl) {
    HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

    return builder
        .baseUrl(intelligenceUrl)
        .requestFactory(new JdkClientHttpRequestFactory(httpClient))
        .build();
  }
}
