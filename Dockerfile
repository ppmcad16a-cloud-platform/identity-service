# ============================================================
# Identity Service - container image (Java 21 / Spring Boot 4)
#
# The JAR is built by CI (mvn verify, with tests + coverage
# gate) and handed to this Dockerfile, so the image needs only
# a Java *runtime* (JRE), no Maven or compiler.
#
#   Local build:  ./mvnw -B package -DskipTests
#                 docker build -t identity-service:local .
#
# Security / operations:
#   - runs as an unprivileged user (uid 10001), not root
#   - small Alpine-based JRE image
#   - JVM sized from the container memory limit, not the host's
#   - exec form ENTRYPOINT so SIGTERM reaches the JVM
#     (Spring Boot then shuts down gracefully)
# ============================================================

FROM eclipse-temurin:21-jre-alpine

# Install the latest Alpine security patches. The base image is only rebuilt
# every few weeks; this closes already-fixed OS CVEs (e.g. brotli) right away.
RUN apk upgrade --no-cache

RUN addgroup -S -g 10001 app && adduser -S -u 10001 -G app -H -s /sbin/nologin app

WORKDIR /app

# Exactly one runnable JAR is produced under target/.
COPY --chown=app:app target/*.jar app.jar

USER 10001
EXPOSE 3101

# MaxRAMPercentage: use up to 75% of the container's memory limit for the
#   heap (the rest is needed for metaspace, threads, buffers).
# ExitOnOutOfMemoryError: crash fast on OOM so Kubernetes restarts the pod
#   instead of leaving a half-broken JVM running.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"

HEALTHCHECK --interval=15s --timeout=3s --start-period=40s --retries=3 \
  CMD wget -qO- http://127.0.0.1:3101/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
