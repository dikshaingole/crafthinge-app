# syntax=docker/dockerfile:1.7
# Build from the REPOSITORY ROOT (the service needs common-lib):
#   docker build -f restaurant-service/Dockerfile -t restaurant-service:local .

# ---------- stage 1: build with Maven (JDK + Maven, discarded after build) ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
# POMs first: this layer only changes when dependencies change
COPY pom.xml .
COPY common-lib/pom.xml common-lib/
COPY user-service/pom.xml user-service/
COPY restaurant-service/pom.xml restaurant-service/
COPY food-service/pom.xml food-service/
COPY cart-service/pom.xml cart-service/
COPY order-service/pom.xml order-service/
COPY payment-service/pom.xml payment-service/
COPY delivery-service/pom.xml delivery-service/
COPY notification-service/pom.xml notification-service/
COPY common-lib/src common-lib/src
COPY restaurant-service/src restaurant-service/src
# Tests run in the CI "Unit Test" stage; the image build only packages.
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -pl restaurant-service -am package -DskipTests
# Split the jar into layers: dependencies change rarely, our code often,
# so after a code change only the small "application" layer is pushed/pulled.
RUN java -Djarmode=tools -jar restaurant-service/target/app.jar extract --layers --launcher --destination /extracted

# ---------- stage 2: runtime (JRE only, no shell, no package manager, non-root) ----------
FROM gcr.io/distroless/java21-debian12:nonroot
WORKDIR /app
COPY --from=build /extracted/dependencies/ ./
COPY --from=build /extracted/spring-boot-loader/ ./
COPY --from=build /extracted/snapshot-dependencies/ ./
COPY --from=build /extracted/application/ ./
USER nonroot
EXPOSE 8080
# MaxRAMPercentage: heap follows the container memory limit set in Kubernetes
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-XX:+ExitOnOutOfMemoryError", "org.springframework.boot.loader.launch.JarLauncher"]
