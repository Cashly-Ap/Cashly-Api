# Multi-stage build for production

# Stage 1: Build the application
FROM maven:3.9.9-amazoncorretto-24 AS builder

WORKDIR /app

RUN yum update -y && yum install -y tar gzip wget && yum clean all

COPY pom.xml ./
COPY .mvn .mvn
COPY mvnw ./

RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B

COPY src ./src

RUN ./mvnw clean package -DskipTests -Dmaven.test.skip=true

# Stage 2: Runtime image
FROM amazoncorretto:24-alpine

WORKDIR /app

RUN apk add --no-cache wget

RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=builder /app/target/*.jar app.jar

RUN mkdir -p /app/uploads

RUN chown -R spring:spring /app

USER spring:spring

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:${PORT:-8080}/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java \
    -XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:+UseG1GC \
    -XX:+UseStringDeduplication \
    -XX:+OptimizeStringConcat \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.profiles.active=production \
    -jar app.jar"]
