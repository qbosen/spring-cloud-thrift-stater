FROM openjdk:8-jdk-alpine
ADD build/libs/calculator-client-1.0.0-SNAPSHOT.jar calculator-client.jar
ENTRYPOINT ["java", "-jar", "calculator-client.jar"]