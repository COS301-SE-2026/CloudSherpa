#!/bin/bash

# service
curl https://start.spring.io/starter.zip \
  -d type=maven-project \
  -d language=java \
  -d bootVersion=3.5.7 \
  -d groupId=com.cloudsherpa \
  -d artifactId=service \
  -d name=service \
  -d packageName=com.cloudsherpa.service \
  -d javaVersion=21 \
  -d dependencies=web,actuator,data-jpa,postgresql \
  -o service.zip

unzip service.zip -d ../apps/service
rm service.zip

# ingestion
curl https://start.spring.io/starter.zip \
  -d type=maven-project \
  -d language=java \
  -d bootVersion=3.5.7 \
  -d groupId=com.cloudsherpa \
  -d artifactId=ingestion \
  -d name=ingestion \
  -d packageName=com.cloudsherpa.ingestion \
  -d javaVersion=21 \
  -d dependencies=web,actuator,data-jpa,postgresql \
  -o ingestion.zip

unzip ingestion.zip -d ../apps/ingestion
rm ingestion.zip
