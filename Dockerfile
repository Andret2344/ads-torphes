FROM openjdk:21-slim
LABEL org.opencontainers.image.authors="Andret2344"
COPY build/libs/ads-torphes.jar torphes.jar
ENTRYPOINT ["java", "-jar", "/torphes.jar"]
