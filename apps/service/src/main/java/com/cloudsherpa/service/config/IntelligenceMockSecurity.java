package com.cloudsherpa.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

@Configuration
@Profile("!prod")
public class IntelligenceMockSecurity {

  @Bean
  WebSecurityCustomizer intelligenceMockWebSecurityCustomizer() {
    return web -> web.ignoring().requestMatchers("/intelligence/mock/**");
  }
}
