plugins {
    kotlin
    `publish-maven`
}

dependencies {
    api("com.faendir.om:parser:2.0.3")
    implementation(project(":scriptdef"))
    api(kotlin("scripting-jvm"))
    api(kotlin("scripting-jvm-host"))
    api(kotlin("scripting-common"))
    api(kotlin("scripting-compiler-embeddable"))
}

publishing {
    publications {
        getByName<MavenPublication>("maven") {
            pom {
                name.set("dsl")
                description.set("Opus Magnum Solution Dsl")
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