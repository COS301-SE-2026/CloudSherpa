package com.cloudsherpa.service.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

// Turns on caching annotation support
// Configuration class as opposed to annotation on main application class to make testing easier as
// per
// https://docs.spring.io/spring-boot/reference/io/caching.html
@Configuration
@EnableCaching
public class CacheConfig {}
