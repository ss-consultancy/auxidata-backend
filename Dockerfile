# ---------- Build stage ----------
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy Maven metadata first so dependencies can be cached.
COPY pom.xml .

# Download dependencies.
RUN mvn -B dependency:go-offline

# Copy backend source code.
COPY src ./src

# Build the Spring Boot executable JAR.
RUN mvn -B clean package -DskipTests


# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Create and use a non-root runtime user.
RUN groupadd --system spring \
    && useradd --system --gid spring spring

COPY --from=build /app/target/*.jar app.jar

RUN chown spring:spring app.jar

USER spring

EXPOSE 8070

ENTRYPOINT ["java", "-jar", "/app/app.jar"]