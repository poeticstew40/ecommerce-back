# --- Etapa 1: Compilación con Maven y Java 21 ---
# Usamos una imagen oficial de Maven que incluye Java 21 (Temurin es una excelente distribución de OpenJDK)
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Establecemos el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiamos solo el pom.xml para aprovechar el caché de Docker y acelerar las compilaciones futuras
COPY pom.xml .

# Descargamos todas las dependencias del proyecto
RUN mvn dependency:go-offline

# Copiamos el resto del código fuente de tu aplicación
COPY src ./src

# Compilamos el proyecto para generar el .jar, saltando los tests para un despliegue más rápido
RUN mvn clean package -DskipTests


# --- Etapa 2: Ejecución con JRE 21 ---
# Usamos una imagen ligera de solo el entorno de ejecución de Java 21 para que el contenedor final sea más pequeño
FROM eclipse-temurin:21-jre-jammy

# Establecemos el directorio de trabajo
WORKDIR /app

# Copiamos el .jar generado en la etapa anterior. El nombre viene de tu pom.xml (<artifactId>-<version>.jar)
COPY --from=build /app/target/ecommerce-0.0.1-SNAPSHOT.jar ./app.jar

# Exponemos el puerto 8080, que es el puerto por defecto de Spring Boot
EXPOSE 8080

# Comando para iniciar tu aplicación cuando el contenedor arranque
ENTRYPOINT ["java", "-jar", "app.jar"]