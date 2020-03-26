plugins {
    kotlin("jvm") version "1.3.71"
}

group = "com.faendir.om"
version = "1.0.0"

repositories {
    jcenter()
    maven { setUrl("https://dl.bintray.com/f43nd1r/maven") }
}

dependencies {
    api("com.faendir.om:omsp:1.1.3")
    implementation(project(":scriptdef"))
    implementation(kotlin("stdlib-jdk8"))
    api(kotlin("scripting-jvm-host"))
    implementation(kotlin("scripting-compiler"))
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}