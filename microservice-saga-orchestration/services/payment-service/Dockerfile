FROM openjdk:11.0.1
EXPOSE 8004
ADD ./build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]