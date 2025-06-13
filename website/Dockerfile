FROM eclipse-temurin:24-jre-alpine-3.21 AS builder
WORKDIR /builder
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=tools -jar application.jar extract --layers --destination extracted

FROM eclipse-temurin:24-jre-alpine-3.21
ARG CREATED
ARG VERSION
ARG REVISION
WORKDIR /application
COPY --from=builder /builder/extracted/dependencies/ ./
COPY --from=builder /builder/extracted/spring-boot-loader/ ./
COPY --from=builder /builder/extracted/snapshot-dependencies/ ./
COPY --from=builder /builder/extracted/application/ ./
ARG UID=10001
RUN addgroup -S library && \
    adduser -G library \
    --disabled-password \
    --gecos "" \
    --home "/nonexistent" \
    --shell "/sbin/nologin" \
    --no-create-home \
    --uid "${UID}" \
    library && \
    chown -R library:library /application
USER library:library
LABEL org.opencontainers.image.created="${CREATED}"
LABEL org.opencontainers.image.authors="Alexandre Carbenay"
LABEL org.opencontainers.image.source="https://github.com/Alexandre-Carbenay/library/blob/main/website/Dockerfile"
LABEL org.opencontainers.image.version="${VERSION}"
LABEL org.opencontainers.image.revision="${REVISION}"
LABEL org.opencontainers.image.title="Library - Catalog"
LABEL org.opencontainers.image.description="Public website for the Library system"
LABEL org.opencontainers.image.base.name="eclipse-temurin:21.0.6_7-jre-alpine-3.21"
EXPOSE 8080/tcp
RUN java -XX:ArchiveClassesAtExit=application.jsa -Dspring.context.exit=onRefresh --enable-preview -jar application.jar
ENTRYPOINT ["java", "--enable-preview", "-XX:SharedArchiveFile=application.jsa", "-jar", "application.jar"]
