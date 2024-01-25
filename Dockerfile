FROM maven:3.8.5-openjdk-17-slim AS build
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-slim
COPY --from=build /target/proman-0.0.1-SNAPSHOT.jar proman.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","proman.jar"]
