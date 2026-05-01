# Build image
FROM eclipse-temurin:25 AS build
WORKDIR /app
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package

# Run image
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ARG VERSION=dev
ENV APP_VERSION=$VERSION
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
