FROM amazoncorretto:21-alpine3.20

WORKDIR /

COPY ./build/libs/*-SNAPSHOT.jar spring.jar

CMD java -jar /spring.jar