FROM openjdk:23-jdk
LABEL authors="Vladislav"

COPY target/scrapper*.jar scrapper.jar

EXPOSE 8081

CMD ["java", "-jar", "scrapper.jar"]
