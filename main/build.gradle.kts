plugins {
    kotlin("jvm") version "1.3.70"
}

group = "com.faendir.om"
version = "1.0-SNAPSHOT"

repositories {
    jcenter()
    maven { setUrl("https://dl.bintray.com/f43nd1r/maven") }
    maven { setUrl("https://dl.bintray.com/s1m0nw1/KtsRunner") }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":dsl"))
    implementation(project(":scriptdef"))
    kotlinScriptDef(project(":scriptdef"))
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}