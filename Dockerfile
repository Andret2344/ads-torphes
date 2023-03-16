FROM openjdk:17
MAINTAINER andret.eu
COPY build/libs/torphes-0.1.0.jar torphes.jar
ENTRYPOINT ["java","-jar","/torphes.jar"]
