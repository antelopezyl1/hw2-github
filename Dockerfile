# ---- build ----
FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests package

# ---- runtime ----
FROM eclipse-temurin:21-jre
WORKDIR /app
ENV PORT=8080
EXPOSE 8080
COPY --from=build /workspace/target/*-SNAPSHOT.jar /app/app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
