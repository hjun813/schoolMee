# Build stage
FROM gradle:7.6-jdk17 AS build
WORKDIR /home/gradle/src
COPY --chown=gradle:gradle . .
RUN gradle build --no-daemon -x test

# Run stage
FROM openjdk:17-slim
WORKDIR /app
COPY --from=build /home/gradle/src/build/libs/*.jar app.jar

# Create uploads directory
RUN mkdir -p uploads/photos

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
