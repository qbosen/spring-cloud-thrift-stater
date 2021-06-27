FROM openjdk:8-jdk-alpine
ADD build/libs/calculator-server-1.0.0-SNAPSHOT.jar calculator-server.jar
ENTRYPOINT ["java", "-jar", "calculator-server.jar"]