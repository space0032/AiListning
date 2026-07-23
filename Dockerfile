FROM eclipse-temurin:21-jdk AS builder

WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -q 2>/dev/null || true

COPY src ./src
RUN mvn clean package -DskipTests -Djava.version=21

FROM eclipse-temurin:21-jre

RUN useradd -r -s /bin/false appuser
WORKDIR /app

COPY --from=builder /build/target/*.jar app.jar

RUN chown -R appuser:appuser /app
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
