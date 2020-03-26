plugins {
    kotlin("jvm") version "1.3.70"
    `maven-publish`
}

group = "com.faendir.om"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("scripting-jvm"))
    implementation(kotlin("script-util"))
    implementation(kotlin("compiler"))
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

