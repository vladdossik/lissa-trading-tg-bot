# Этап 1: Сборка приложения
FROM maven:3.8.5-openjdk-17 AS build

# Устанавливаем рабочую директорию внутри контейнера
WORKDIR /app

# Копируем файл pom.xml и любые другие необходимые файлы для загрузки зависимостей
COPY pom.xml .
COPY src ./src

# Копируем кастомную библиотеку в контейнер (если используется)
COPY libs/auth-security-lib-0.0.1-SNAPSHOT.jar /app/libs/

# Устанавливаем кастомную библиотеку в локальный Maven-репозиторий (если используется)
RUN mvn install:install-file -Dfile=/app/libs/auth-security-lib-0.0.1-SNAPSHOT.jar \
    -DgroupId=lissa.trading \
    -DartifactId=auth-security-lib \
    -Dversion=0.0.1-SNAPSHOT \
    -Dpackaging=jar

# Собираем приложение
RUN mvn clean package -DskipTests

# Этап 2: Создание образа для запуска приложения
FROM openjdk:17-jdk-slim

# Устанавливаем рабочую директорию внутри контейнера
WORKDIR /app

# Копируем собранный JAR-файл из предыдущего этапа
COPY --from=build /app/target/*.jar ./app.jar

# Копируем файл настроек приложения
COPY src/main/resources/application.yml ./application.yml

# Открываем порт, если ваше приложение использует его
EXPOSE 8083

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "app.jar"]
