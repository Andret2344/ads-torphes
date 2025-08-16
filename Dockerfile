FROM openjdk:21-slim

LABEL org.opencontainers.image.title="ads-torphes"
LABEL org.opencontainers.image.authors="Andret2344"
LABEL org.opencontainers.image.description="Discord bot 'ads-torphes' written in Java 21, packaged as a runnable JAR and intended to run inside a container."
LABEL org.opencontainers.image.url="https://github.com/Andret2344/ads-torphes"
LABEL org.opencontainers.image.source="https://github.com/Andret2344/ads-torphes"
LABEL org.opencontainers.image.licenses="CC-BY-SA 4.0"


COPY build/libs/ads-torphes.jar torphes.jar
ENTRYPOINT ["java", "-jar", "/torphes.jar"]
