FROM eclipse-temurin:21-jdk
WORKDIR /app
<<<<<<< HEAD
COPY out/artifacts/fliply_jar/fliply.jar fliply.jar
=======
COPY out/artifacts/fliply/fliply.jar app.jar
>>>>>>> 3b69d5ee6f0ad8a9a15f10f674f55eb049cf240c
ENTRYPOINT ["java", "-jar", "fliply.jar"]
