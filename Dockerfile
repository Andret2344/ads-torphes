FROM openjdk:21-slim
LABEL org.opencontainers.image.authors="Andret2344"
WORKDIR /app
COPY build/libs/torphes.jar torphes.jar
ENTRYPOINT ["java", "-jar", "/torphes.jar"]
