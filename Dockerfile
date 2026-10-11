# ---- Stage 1: build the project ----
# Uses a full Java toolkit (JDK) to compile the code with Maven
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
COPY src src
RUN ./mvnw -q package -DskipTests

# ---- Stage 2: run the service ----
# Uses a smaller Java runtime (JRE) and copies in only the finished program
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
