# ==== Base ====
FROM eclipse-temurin:21-jdk AS base
WORKDIR /app

COPY apps/ingestion/mvnw .
COPY apps/ingestion/.mvn .mvn
COPY apps/ingestion/pom.xml apps/ingestion/pom.xml
COPY apps/lib apps/lib
RUN chmod +x mvnw
RUN ./mvnw -f apps/lib/pom.xml clean install -DskipTests
RUN ./mvnw -f apps/ingestion/pom.xml dependency:go-offline

# ==== Dev ====
FROM base AS dev

COPY apps/ingestion apps/ingestion

EXPOSE 8080
ENTRYPOINT ["./mvnw", "spring-boot:run"]


# ==== Build ====
FROM base AS build

COPY apps/ingestion/src apps/ingestion/src
COPY apps/ingestion/config/checkstyle/checkstyle.xml config/checkstyle/checkstyle.xml

RUN ./mvnw -f apps/ingestion/pom.xml clean package -DskipTests


# ==== Prod ====
FROM eclipse-temurin:21-jre AS prod

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

RUN useradd ingestionuser
USER ingestionuser

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]