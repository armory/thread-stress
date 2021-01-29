FROM alpine:3.12

ENV KUBECTL_RELEASE=1.18.10
ENV RATE_MS=50
ENV SLEEP_SEC=1
ENV LD_LIBRARY_PATH=/lib64

RUN apk update \
  && apk upgrade \
  && apk --no-cache add --update \
    bash \
    ca-certificates \
    wget \
    curl \
    openjdk11 \
    libc6-compat

# kubectl
RUN wget https://storage.googleapis.com/kubernetes-release/release/v${KUBECTL_RELEASE}/bin/linux/amd64/kubectl \
  && chmod +x kubectl \
  && mv ./kubectl /usr/local/bin/kubectl

# YourKit
RUN wget https://www.yourkit.com/download/docker/YourKit-JavaProfiler-2020.9-docker.zip -P /tmp/ && \
  unzip /tmp/YourKit-JavaProfiler-2020.9-docker.zip -d /usr/local && \
  rm /tmp/YourKit-JavaProfiler-2020.9-docker.zip

WORKDIR /tmp

COPY build/libs/thread-stress.jar /tmp

CMD ["java", "-agentpath:/usr/local/YourKit-JavaProfiler-2020.9/bin/linux-x86-64/libyjpagent.so=port=10001,listen=all", "-jar", "/tmp/thread-stress.jar", "$RATE_MS", "$SLEEP_SEC"]
