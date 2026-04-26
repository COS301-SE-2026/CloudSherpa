# ==== Stage 1: Build ====
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app


COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN chmod +x mvnw
# Download dependencies before copying source code -> faster builds.
# Why? When source code changes and you run docker compose build, it will
# rebuild the image from the point of changes. When we copy source code after
# installing dependencies, we ensure that dependencies do not get downloaded again
# whenever src changes.
RUN ./mvnw dependency:go-offline

COPY src src

# Build the jar file while skipping tests
# clean deletes old target/ directory
RUN ./mvnw clean package -DskipTests

# ==== Stage 2: Runtime ====
# Smaller runtime image as opposed to larger JDK image
FROM eclipse-temurin:21-jre

WORKDIR /app

# Jar already built during build step, copy to image and run
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
