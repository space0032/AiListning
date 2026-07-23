FROM eclipse-temurin:21-jre AS runtime-base

ARG JAVA_VERSION=26

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/* && \
    curl -sL https://download.java.net/java/GA/jdk${JAVA_VERSION}.1/865c1da2e1e24da3b1e59b7a9da48c61/36/GPL/openjdk-${JAVA_VERSION}_linux-x64_bin.tar.gz \
    | tar -xzC /opt && \
    ln -s /opt/jdk-${JAVA_VERSION} /opt/java

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
