FROM gradle:jdk21 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN git config --global --add safe.directory /home/gradle/src
RUN gradle build
RUN unzip -d /app build/distributions/gtfsvtor.zip
RUN mv /app/*-* /app/gtfsvtor

FROM openjdk:21-jdk-slim
LABEL maintainer="Holger Bruch holger.bruch@mitfahrdezentrale.de"

COPY --from=build /app /app
WORKDIR /data

ENV GTFSVTOR_OPTS=-Xmx4G

ENTRYPOINT ["/app/gtfsvtor/bin/gtfsvtor"]
CMD ["-h"]
