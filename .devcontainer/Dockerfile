FROM ubuntu:24.04

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y \
    curl wget gnupg2 ca-certificates apt-transport-https \
    git sudo \
    && rm -rf /var/lib/apt/lists/*

# Java 26
RUN wget -qO- https://download.java.net/java/GA/jdk26.1/865c1da2e1e24da3b1e59b7a9da48c61/36/GPL/openjdk-26_linux-x64_bin.tar.gz \
    | tar -xzC /opt \
    && ln -s /opt/jdk-26 /opt/java \
    && rm -rf /tmp/*

ENV JAVA_HOME=/opt/java
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# Maven 3.9.9
RUN MAVEN_VERSION=3.9.9 && \
    wget -qO /tmp/maven.tar.gz "https://dlcdn.apache.org/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" && \
    tar -xzf /tmp/maven.tar.gz -C /opt && \
    ln -s /opt/apache-maven-${MAVEN_VERSION} /opt/maven && \
    rm /tmp/maven.tar.gz

ENV MAVEN_HOME=/opt/maven
ENV PATH="${MAVEN_HOME}/bin:${PATH}"

# Node.js 20
RUN curl -fsSL https://deb.nodesource.com/setup_20.x | bash - \
    && apt-get install -y nodejs \
    && rm -rf /var/lib/apt/lists/*

# Non-root user
RUN useradd -m -s /bin/bash vscode && \
    echo "vscode ALL=(ALL) NOPASSWD:ALL" >> /etc/sudoers

USER vscode
WORKDIR /workspace
