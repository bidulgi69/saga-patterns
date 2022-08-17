FROM openjdk:11.0.1
EXPOSE 8001
ADD ./build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]