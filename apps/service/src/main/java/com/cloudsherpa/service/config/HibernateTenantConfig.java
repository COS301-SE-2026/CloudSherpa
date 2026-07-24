package com.cloudsherpa.service.config;

import java.util.Map;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HibernateTenantConfig {

  @Bean
  HibernatePropertiesCustomizer tenantHibernatePropertiesCustomizer(
      SchemaMultiTenantConnectionProvider connectionProvider, TenantResolver tenantResolver) {
    return (Map<String, Object> hibernateProperties) -> {
      hibernateProperties.put(
          AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, connectionProvider);
      hibernateProperties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, tenantResolver);
    };
  }
}
