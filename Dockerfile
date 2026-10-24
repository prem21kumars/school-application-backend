# ---- Build Stage ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Cache Maven dependencies
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN chmod +x mvnw 2>/dev/null || true
RUN ./mvnw dependency:go-offline -B || true

# Copy source and package
COPY src ./src
RUN ./mvnw clean package -DskipTests

# ---- Runtime Stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# JVM and container options
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Copy the built jar
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
