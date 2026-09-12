# --- STAGE 1: Cache de Dependências (Base) ---
FROM maven:3.9.6-eclipse-temurin-21-alpine AS dependencies
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B

# --- STAGE 2: Ambiente de Desenvolvimento (Usado pelo Docker Watch) ---
FROM dependencies AS dev
WORKDIR /app
COPY src ./src
EXPOSE 8080
# Roda via Maven para recompilar o código a cada reinicialização
CMD ["mvn", "spring-boot:run"]

# --- STAGE 3: Build do pacote de Produção ---
FROM dependencies AS builder
WORKDIR /app
COPY src ./src
RUN mvn package

# --- STAGE 4: Imagem final de Produção (Leve e Segura) ---
FROM eclipse-temurin:21-jre-alpine AS prod
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]