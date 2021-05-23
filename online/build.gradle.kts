plugins {
    kotlin
    id("org.springframework.boot") version "2.5.0"
    id("com.devsoap.vaadin-flow") version "1.5.1"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("com.palantir.docker") version "0.26.0"
}

version = "1.1.0"

vaadin {
    version = "14.5.4"
}

repositories {
    maven { setUrl("https://maven.vaadin.com/vaadin-addons") }
    maven { setUrl("https://oss.sonatype.org/content/repositories/vaadin-snapshots") }
    maven { setUrl("https://maven.vaadin.com/vaadin-prereleases") }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.71")
    implementation("com.faendir.om:dsl:1.1.5")
    implementation(vaadin.bom())
    implementation(vaadin.core())
    implementation(vaadin.dependency("spring-boot-starter"))
    implementation("com.juicy:juicyaceeditor:1.0.9")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

val unpackBootJar = tasks.register<Copy>("unpackBootJar") {
    //from(zipTree(tasks.findByName("bootJar")?.outputs?.files?.get(0)))
    into("$buildDir/dependency")
    dependsOn("bootJar")
}

docker {
    name = "f43nd1r/omsekt:latest"
    //files(unpackBootJar.outputs, "java.policy")
    //dependsOn(unpackBootJar)
}
