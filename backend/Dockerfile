FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apk add maven && mvn clean package -DskipTests
EXPOSE 8080
CMD ["java", "-jar", "target/backend-1.0.0.jar"]
