FROM maven:3.6.3-jdk-11-openj9 as builder

WORKDIR usr/app
COPY pom.xml .
COPY src ./src
RUN mvn package -Dmaven.test.skip

FROM adoptopenjdk/openjdk11:jre

COPY --from=builder usr/app/target/user-service-0.0.1-SNAPSHOT.jar /user-service-0.0.1-SNAPSHOT.jar

ENTRYPOINT ["java", "-jar", "user-service-0.0.1-SNAPSHOT.jar"]
