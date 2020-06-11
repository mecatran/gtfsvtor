FROM gradle:jdk11 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build

FROM openjdk:11-jre
LABEL maintainer="Holger Bruch holger.bruch@mitfahrdezentrale.de"

COPY --from=build /home/gradle/src/build/distributions/* /app/
RUN unzip -d /app /app/gtfsvtor.zip
WORKDIR /data

ENV GTFSVTOR_OPTS=-Xmx4G

ENTRYPOINT ["/app/gtfsvtor/bin/gtfsvtor"]
CMD ["-h"]