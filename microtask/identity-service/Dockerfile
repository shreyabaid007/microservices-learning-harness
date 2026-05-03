# syntax=docker/dockerfile:1
# ---------- build stage ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml ./
COPY identity-service/pom.xml identity-service/pom.xml
COPY task-service/pom.xml task-service/pom.xml
RUN mvn -B -pl identity-service -am dependency:go-offline -DskipTests

COPY identity-service/src identity-service/src
RUN mvn -B -pl identity-service -am package -DskipTests \
 && cp identity-service/target/identity-service-*.jar /workspace/app.jar

# ---------- runtime stage ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd -r -u 1001 appuser
COPY --from=build /workspace/app.jar /app/app.jar
USER appuser
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
