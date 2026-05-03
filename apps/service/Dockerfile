# ==== Base ====
FROM eclipse-temurin:21-jdk AS base
WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

# ==== Dev ====
FROM base AS dev

COPY . .

EXPOSE 8080
ENTRYPOINT ["./mvnw", "spring-boot:run"]


# ==== Build ====
FROM base AS build

COPY src src
RUN ./mvnw clean package -DskipTests


# ==== Prod ====
FROM eclipse-temurin:21-jre AS prod

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]