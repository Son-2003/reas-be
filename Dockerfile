# Build Stage
FROM openjdk:21-jdk-slim AS build

RUN apt-get update && apt-get install -y maven
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Runtime Stage
FROM openjdk:21-jdk-slim
WORKDIR /app

# Copy JAR file from the build stage
COPY --from=build /app/target/reas-be-0.0.1-SNAPSHOT.jar reas-be.jar

# Copy Firebase config file to container
COPY --from=build /app/src/main/resources/firebase/reas-app-f46cc-firebase-adminsdk-fbsvc-94587df6b3.json /app/firebase/reas-app-f46cc-firebase-adminsdk-fbsvc-94587df6b3.json

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "reas-be.jar"]
