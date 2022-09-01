FROM maven:3.8.3-openjdk-17 as MAVEN_BUILD
COPY pom.xml /tmp/
RUN mvn dependency:go-offline -f /tmp/pom.xml
COPY src /tmp/src/
WORKDIR /tmp/
RUN mvn package -Dmaven.test.skip=true
FROM eclipse-temurin:17-jre-alpine as RUN_APP
EXPOSE 8080
RUN mkdir /app
COPY --from=MAVEN_BUILD /tmp/target/*.jar /app/application.jar
ENTRYPOINT ["java","-jar","/app/application.jar"]