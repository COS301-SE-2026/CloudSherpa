#!/bin/bash

# ! Important note: This script is not written with multiple runs in mind
# it is included for transparency of how the applications were initialized

# Script used for initialization of Spring Boot apps
# - ingestion service
# - analytics-engine
# - normalization-service

# DEPENDENCIES (Subject to pom.xml changes as needs evolve)
# web
# - Embedded web server (Tomcat)
# - REST API support
# - JSON serialization (Jackson)
# - HTTP routing (@GetMapping, etc.)
# actuator
# - Production monitoring endpoints
# - /actuator/health -> is service alive?
# - /actuator/metrics -> performance stats
# - /actuator/info -> metadata
# kafka
# - Kafka producers
# - Kafka consumers
# - Serialization/deserialization
# - Listener abstraction (@KafkaListener)

# ingestion-service
curl https://start.spring.io/starter.zip \
  -d type=maven-project \
  -d language=java \
  -d bootVersion=3.5.7 \
  -d groupId=com.cloudsherpa \
  -d artifactId=ingestion-service \
  -d name=ingestion-service \
  -d packageName=com.cloudsherpa.ingestion \
  -d javaVersion=21 \
  -d dependencies=web,actuator,kafka \
  -o ingestion-service.zip

unzip ingestion-service.zip -d ../apps/ingestion-service
rm ingestion-service.zip

# analytics-engine
curl https://start.spring.io/starter.zip \
  -d type=maven-project \
  -d language=java \
  -d bootVersion=3.5.7 \
  -d groupId=com.cloudsherpa \
  -d artifactId=analytics-engine \
  -d name=analytics-engine \
  -d packageName=com.cloudsherpa.analytics \
  -d javaVersion=21 \
  -d dependencies=web,actuator,kafka \
  -o analytics-engine.zip

unzip analytics-engine.zip -d ../apps/analytics-engine
rm analytics-engine.zip

# normalization-service
curl https://start.spring.io/starter.zip \
  -d type=maven-project \
  -d language=java \
  -d bootVersion=3.5.7 \
  -d groupId=com.cloudsherpa \
  -d artifactId=normalization-service \
  -d name=normalization-service \
  -d packageName=com.cloudsherpa.normalization \
  -d javaVersion=21 \
  -d dependencies=web,actuator,kafka \
  -o normalization-service.zip

unzip normalization-service.zip -d ../apps/normalization-service
rm normalization-service.zip