FROM openjdk:23-jdk
LABEL authors="Vladislav"

COPY target/bot*.jar bot.jar

EXPOSE 8080

CMD ["java", "-jar", "bot.jar"]
