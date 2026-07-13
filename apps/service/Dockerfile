# ==== Base ====
FROM eclipse-temurin:21-jdk AS base
WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY apps/service/pom.xml apps/service/pom.xml
COPY apps/lib/pom.xml apps/lib/pom.xml
RUN chmod +x mvnw
COPY apps/lib apps/lib
RUN ./mvnw -f apps/lib/pom.xml install -DskipTests
RUN ./mvnw -f apps/service/pom.xml dependency:go-offline

# ==== Dev ====
FROM base AS dev

COPY apps/service apps/service

EXPOSE 8080
ENTRYPOINT ["./mvnw", "spring-boot:run"]


# ==== Build ====
FROM base AS build

COPY apps/service/src apps/service/src
COPY apps/service/config/checkstyle/checkstyle.xml apps/service/config/checkstyle/checkstyle.xml
RUN ./mvnw -f apps/service/pom.xml clean package -DskipTests


# ==== Prod ====
FROM eclipse-temurin:21-jre AS prod

WORKDIR /app
COPY --from=build /app/apps/service/target/*.jar app.jar

RUN useradd serviceuser
USER serviceuser 

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
