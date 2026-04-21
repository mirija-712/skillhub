# Image runtime du service d'authentification Spring Boot (SkillHub).
# Étape build : Maven compile le JAR. Étape finale : JRE seule, utilisateur non-root.

FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

# Pas de compilation des sources de test dans l’image (-Dmaven.test.skip) ; les tests restent en CI.
RUN mvn -q -Dmaven.test.skip=true package -B

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -g 1001 spring && adduser -u 1001 -G spring -h /app -D spring

COPY --from=build /app/target/*.jar /app/app.jar
RUN chown spring:spring /app/app.jar

USER spring
EXPOSE 8080

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
