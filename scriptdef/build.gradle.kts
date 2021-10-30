plugins {
    kotlin
    `publish-maven`
}

dependencies {
    implementation(kotlin("scripting-jvm"))
}

publishing {
    publications {
        getByName<MavenPublication>("maven") {
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
