FROM amazoncorretto:21.0.4-alpine3.20
WORKDIR /app
COPY build/libs/*SNAPSHOT.jar app.jar

EXPOSE 8080 9010

ENV JAVA_OPTS="-Dcom.sun.management.jmxremote \
                -Dcom.sun.management.jmxremote.port=9010 \
                -Dcom.sun.management.jmxremote.rmi.port=9010 \
                -Dcom.sun.management.jmxremote.authenticate=false \
                -Dcom.sun.management.jmxremote.ssl=false \
                -Djava.rmi.server.hostname=172.17.0.1 \
                -Dcom.sun.management.jmxremote.local.only=false"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]