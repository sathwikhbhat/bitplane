FROM eclipse-temurin:21-jdk-alpine AS build

RUN apk add --no-cache maven

WORKDIR /app

COPY . .

RUN mvn package -DskipTests

FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache ffmpeg

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
