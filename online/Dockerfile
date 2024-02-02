FROM alpine:latest as builder

RUN apk --no-cache add openjdk17-jdk openjdk17-jmods binutils
ENV JAVA_MINIMAL="/opt/java-minimal"
RUN /usr/lib/jvm/java-17-openjdk/bin/jlink \
    --verbose \
    --add-modules \
java.base,\
java.compiler,\
java.datatransfer,\
java.desktop,\
java.instrument,\
java.logging,\
java.management,\
java.naming,\
java.net.http,\
java.prefs,\
java.rmi,\
java.scripting,\
java.security.jgss,\
java.sql,\
java.xml,\
jdk.attach,\
jdk.compiler,\
jdk.jdi,\
jdk.jfr,\
jdk.unsupported,\
jdk.jdwp.agent,\
jdk.zipfs \
    --compress 2 --strip-debug --no-header-files --no-man-pages \
    --release-info="add:IMPLEMENTOR=f43nd1r:IMPLEMENTOR_VERSION=f43nd1r_JRE" \
    --output "$JAVA_MINIMAL"

WORKDIR application
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM alpine:latest

ENV JAVA_HOME=/opt/java-minimal
ENV PATH="$PATH:$JAVA_HOME/bin"
COPY --from=builder "$JAVA_HOME" "$JAVA_HOME"

WORKDIR application
COPY --from=builder application/dependencies/ ./
RUN true
COPY --from=builder application/spring-boot-loader/ ./
RUN true
COPY --from=builder application/snapshot-dependencies/ ./
RUN true
COPY --from=builder application/application/ ./
COPY java.policy java.policy

RUN uid=$(($(($((`date +%s` / 60)) % 165535)) + 100000)) && adduser -D -h /application/ -u $uid f43dit && chown -R f43dit:f43dit /application
USER f43dit

ENTRYPOINT ["java","-Djava.security.manager", "-Djava.security.policy==java.policy", "org.springframework.boot.loader.launch.JarLauncher"]