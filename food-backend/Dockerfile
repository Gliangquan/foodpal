FROM maven:3.9.9-eclipse-temurin-11 AS builder
WORKDIR /app
COPY pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:11-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 9901
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
