FROM gradle:jdk11 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build
RUN unzip -d /app build/distributions/gtfsvtor.zip

FROM openjdk:11-jre-slim
LABEL maintainer="Holger Bruch holger.bruch@mitfahrdezentrale.de"

COPY --from=build /app /app
WORKDIR /data

ENV GTFSVTOR_OPTS=-Xmx4G

ENTRYPOINT ["/app/gtfsvtor/bin/gtfsvtor"]
CMD ["-h"]