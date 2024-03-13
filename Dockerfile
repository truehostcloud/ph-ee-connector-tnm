FROM eclipse-temurin:17-alpine AS build

WORKDIR /ph-ee-connector-tnm

COPY . .

RUN ./gradlew bootJar

FROM eclipse-temurin:17-alpine

WORKDIR /app

COPY --from=build /ph-ee-connector-tnm/build/libs/ph-ee-connector-tnm*.jar .

EXPOSE 5000

ENTRYPOINT ["java", "-jar", "/app/ph-ee-connector-tnm.jar"]