# ---------- Estagio 1: build ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache de dependencias: copia o pom primeiro
COPY pom.xml .
RUN mvn -q dependency:go-offline

# Copia o codigo e empacota (pula testes na imagem; rode-os no CI/local)
COPY src ./src
RUN mvn -q clean package -DskipTests

# ---------- Estagio 2: runtime ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Usuario nao-root por seguranca
RUN addgroup -S app && adduser -S app -G app
USER app

COPY --from=build /app/target/agrocontrol.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
