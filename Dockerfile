# ============================================================
# Identity Server Docker Image
# ============================================================


# ------------------------------------------------------------
# Runtime image
#
# Maven compilation already happens in GitHub Actions.
# Therefore Docker does NOT need Maven or the JDK compiler.
#
# We only need a Java runtime to execute the packaged JAR.
# ------------------------------------------------------------
FROM eclipse-temurin:21-jre


# ------------------------------------------------------------
# Application directory inside container
# ------------------------------------------------------------
WORKDIR /app


# ------------------------------------------------------------
# Copy the JAR produced by Maven into the container.
#
# IMPORTANT:
# This assumes one runnable JAR exists under target/.
#
# Later we can make the JAR name deterministic in pom.xml.
# ------------------------------------------------------------
COPY target/*.jar app.jar


# ------------------------------------------------------------
# Application port
#
# Change this if Identity Server uses another port.
# ------------------------------------------------------------
EXPOSE 3101


# ------------------------------------------------------------
# Start Spring Boot application
# ------------------------------------------------------------
ENTRYPOINT ["java", "-jar", "app.jar"]