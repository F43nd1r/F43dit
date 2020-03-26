FROM openjdk:13-slim
RUN groupadd spring && useradd spring -g spring
USER spring:spring
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
COPY java.policy java.policy
ENTRYPOINT ["java","-jar","/app.jar", "-Djava.security.manager", "-Djava.security.policy==java.policy"]