FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY out/artifacts/fliply_jar/fliply.jar fliply.jar
ENTRYPOINT ["java", "-jar", "fliply.jar"]
