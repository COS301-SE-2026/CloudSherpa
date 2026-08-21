package com.cloudsherpa.ingestion.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringContextBridge implements ApplicationContextAware {

  private static ApplicationContext context;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    context = applicationContext; // NOSONAR Cannot make enclosing scope static due to implementing
    // interface method
  }

  // Static helper method to fetch beans by class type
  public static <T> T getBean(Class<T> beanClass) {
    return context.getBean(beanClass);
  }

  // Static helper method to fetch beans by name
  public static Object getBean(String beanName) {
    return context.getBean(beanName);
  }
}
