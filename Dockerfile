# ---------- Build stage ----------
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy Maven configuration first so dependency downloads can be cached.
COPY pom.xml .

# Download Maven dependencies.
RUN mvn -B dependency:go-offline

# Copy application source code.
COPY src ./src

# Create the executable Spring Boot JAR.
RUN mvn -B clean package -DskipTests


# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Use a non-root user in the runtime container.
RUN groupadd --system spring \
    && useradd --system --gid spring spring

COPY --from=build /app/target/*.jar app.jar

RUN chown spring:spring app.jar

USER spring

# Railway supplies PORT at runtime; EXPOSE is only documentation.
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]