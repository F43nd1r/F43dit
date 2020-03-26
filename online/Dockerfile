FROM openjdk:13-slim
RUN groupadd spring && useradd spring -g spring
USER spring:spring
ARG DEPENDENCY=build/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app
COPY java.policy java.policy
ENTRYPOINT ["java","-cp","app:app/lib/*", "-Djava.security.manager", "-Djava.security.policy==java.policy", "com.faendir.om.online.Application"]