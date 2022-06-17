FROM adoptopenjdk/openjdk11:latest
EXPOSE 8080
COPY target/proxy-0.0.1-SNAPSHOT.jar proxy-0.0.1-SNAPSHOT.jar
CMD ["java", "-jar", "proxy-0.0.1-SNAPSHOT.jar"]