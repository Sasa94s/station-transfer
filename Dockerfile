FROM openjdk:26-ea-jdk-slim
WORKDIR /app
COPY target/station-transfer-0.0.1.jar station-transfer-0.0.1.jar
ENTRYPOINT ["java", "-jar", "station-transfer-0.0.1.jar"]