FROM eclipse-temurin:25-jre-alpine AS builder
WORKDIR /builder
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=tools -jar application.jar extract --layers --destination extracted

FROM eclipse-temurin:25-jre-alpine
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
LABEL org.opencontainers.image.source="https://github.com/Alexandre-Carbenay/library/blob/main/catalog/Dockerfile"
LABEL org.opencontainers.image.version="${VERSION}"
LABEL org.opencontainers.image.revision="${REVISION}"
LABEL org.opencontainers.image.title="Library - Catalog"
LABEL org.opencontainers.image.description="Catalog service for the Library system"
LABEL org.opencontainers.image.base.name="eclipse-temurin:25-jre-alpine"
EXPOSE 8080/tcp
RUN java -XX:ArchiveClassesAtExit=application.jsa -Dspring.context.exit=onRefresh -jar application.jar
ENTRYPOINT ["java", "-XX:SharedArchiveFile=application.jsa", "-jar", "application.jar"]
