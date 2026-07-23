FROM ubuntu:24.04 AS runtime-base

ENV DEBIAN_FRONTEND=noninteractive
ENV JAVA_HOME=/opt/java
ENV PATH="${JAVA_HOME}/bin:${PATH}"

RUN apt-get update && apt-get install -y curl maven && rm -rf /var/lib/apt/lists/*

RUN curl -sL "https://github.com/adoptium/temurin26-binaries/releases/download/jdk-26.0.1%2B8/OpenJDK26U-jdk_x64_linux_hotspot_26.0.1_8.tar.gz" \
    | tar -xzC /opt && \
    mv /opt/jdk-26.0.1+8 /opt/java

FROM runtime-base AS builder

WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -q 2>/dev/null || true

COPY src ./src
RUN mvn clean package -DskipTests -Djava.version=21

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
