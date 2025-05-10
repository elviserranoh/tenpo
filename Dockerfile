FROM amazoncorretto:21.0.4-alpine3.20
WORKDIR /app
COPY build/libs/*SNAPSHOT.jar app.jar
EXPOSE 9000
ENTRYPOINT ["java", "-jar", "app.jar"]