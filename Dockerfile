# Usar a imagem oficial do OpenJDK 17 como base
FROM openjdk:17-jdk-slim as build

# Definir o diretório de trabalho
WORKDIR /app

# Copiar o arquivo JAR da aplicação para o container
COPY target/*.jar app.jar

# Expor a porta que a aplicação irá rodar
EXPOSE 8080

# Comando para rodar a aplicação
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
