# Используем базовый образ с JDK 17
FROM eclipse-temurin:17-jdk-alpine

# Устанавливаем рабочую директорию внутри контейнера
WORKDIR /app

# Копируем файл jar в контейнер
COPY target/tg-bot-0.0.1-SNAPSHOT.jar /app/service.jar

# Открываем порт, который будет использоваться приложением
EXPOSE 8083

# Команда для запуска приложения
ENTRYPOINT ["java","-jar","/app/service.jar"]