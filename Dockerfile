# =========================================================================
#  ETAPA 1: LA FABRICA (Donde construimos la aplicacion)
# =========================================================================
FROM maven:3.8-openjdk-17 AS build

WORKDIR /app

# Copiar pom.xml desde la subcarpeta images-api
COPY images-api/pom.xml .

# Descargar dependencias
RUN mvn dependency:go-offline

# Copiar codigo fuente desde la subcarpeta images-api
COPY images-api/src ./src

# Compilar y empaquetar
RUN mvn package -DskipTests


# =========================================================================
#  ETAPA 2: LA CAJA DEL PRODUCTO (Donde ejecutamos la aplicacion)
# =========================================================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Crear usuario no-root para seguridad
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copiar el JAR desde la etapa de build
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Configuracion optimizada para contenedores
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
