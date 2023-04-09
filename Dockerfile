# syntax=docker/dockerfile:1

FROM eclipse-temurin:8-jdk-alpine as base
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:resolve
COPY src ./src

FROM base as development
CMD ["./mvnw", "compile", "exec:java"]

FROM base as build
RUN ./mvnw package
