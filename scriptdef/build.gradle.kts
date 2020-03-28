plugins {
    kotlin("jvm") version "1.3.70"
    `maven-publish`
}

group = "com.faendir.om"
version = "1.0.2"

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

publishing {
    repositories {
        mavenLocal()
        maven {
            setUrl("https://api.bintray.com/maven/f43nd1r/maven/scriptdef/;publish=1")
            name = "bintray"
            credentials {
                username = findProperty("artifactoryUser") as String?
                password = findProperty("artifactoryApiKey") as String?
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            from(components["kotlin"])
            pom {
                name.set("scriptdef")
                description.set("Opus Magnum Solution Script Definition")
                url.set("https://github.com/F43nd1r/omsekt")

                scm {
                    connection.set("scm:git:https://github.com/F43nd1r/omsekt.git")
                    developerConnection.set("scm:git:git@github.com:F43nd1r/omsekt.git")
                    url.set("https://github.com/F43nd1r/omsekt.git")
                }

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        distribution.set("repo")
                    }
                }

                developers {
                    developer {
                        id.set("f43nd1r")
                        name.set("Lukas Morawietz")
                    }
                }
            }
        }
    }
}
