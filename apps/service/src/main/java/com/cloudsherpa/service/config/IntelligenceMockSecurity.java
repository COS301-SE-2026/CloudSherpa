package com.cloudsherpa.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

@Configuration
public class IntelligenceMockSecurity {

  @Bean
  WebSecurityCustomizer intelligenceMockWebSecurityCustomizer() {
    return web -> web.ignoring().requestMatchers("/intelligence/mock/**");
  }
}
