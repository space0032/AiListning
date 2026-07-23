FROM eclipse-temurin:21-jre AS runtime-base

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/* && \
    curl -sL "https://github.com/adoptium/temurin26-binaries/releases/download/jdk-26.0.1%2B8/OpenJDK26U-jdk_x64_linux_hotspot_26.0.1_8.tar.gz" \
    | tar -xzC /opt && \
    JDK_DIR=$(ls -d /opt/jdk-26* | head -1) && \
    ln -s "$JDK_DIR" /opt/java && \
    echo "Extracted JDK to: $JDK_DIR"

ENV JAVA_HOME=/opt/java
ENV PATH="${JAVA_HOME}/bin:${PATH}"

FROM runtime-base AS builder

RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -q 2>/dev/null || true

COPY src ./src
RUN mvn clean package -DskipTests -q

FROM runtime-base

RUN useradd -r -s /bin/false appuser
WORKDIR /app

COPY --from=builder /build/target/*.jar app.jar

RUN chown -R appuser:appuser /app
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
