FROM openjdk:17-oracle
WORKDIR /app
COPY target/*.jar /app/socialnetwork.jar
ENTRYPOINT ["java","-jar","-Dspring.profiles.active=docker","socialnetwork.jar"]
