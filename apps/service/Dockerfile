# ==== Base ====
FROM eclipse-temurin:21-jdk AS base
WORKDIR /app

COPY apps/service/mvnw .
COPY apps/service/.mvn .mvn
COPY apps/service/pom.xml .
COPY apps/lib apps/lib
RUN chmod +x mvnw
RUN ./mvnw -f apps/lib/pom.xml clean install -DskipTests
RUN ./mvnw dependency:go-offline

# ==== Dev ====
FROM base AS dev

COPY . .

EXPOSE 8080
ENTRYPOINT ["./mvnw", "spring-boot:run"]


# ==== Build ====
FROM base AS build

COPY apps/ingestion/src src
COPY apps/ingestion/config/checkstyle/checkstyle.xml config/checkstyle/checkstyle.xml

RUN ./mvnw clean package -DskipTests


# ==== Prod ====
FROM eclipse-temurin:21-jre AS prod

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

RUN useradd serviceuser
USER serviceuser 

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]