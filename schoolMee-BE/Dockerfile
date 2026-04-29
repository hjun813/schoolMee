# build stage
FROM gradle:7.6-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle build -x test --no-daemon

# run stage
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

# Ensure uploads directory exists
RUN mkdir -p uploads/photos

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
