plugins {
    kotlin
    `publish-maven`
}

dependencies {
    api(libs.om.parser)
    implementation(projects.scriptdef)
    api(libs.kotlin.scripting.jvm)
    api(libs.kotlin.scripting.jvmHost)
    api(libs.kotlin.scripting.compiler.embeddable)
    implementation(libs.kotlinpoet)
    implementation(libs.kotlinbard)
    implementation(libs.kotlinx.coroutines)
}

publishing {
    publications {
        getByName<MavenPublication>("maven") {
            pom {
                name.set("dsl")
                description.set("Opus Magnum Solution Dsl")
                url.set("https://github.com/F43nd1r/F43dit")

                scm {
                    connection.set("scm:git:https://github.com/F43nd1r/F43dit.git")
                    developerConnection.set("scm:git:git@github.com:F43nd1r/F43dit.git")
                    url.set("https://github.com/F43nd1r/F43dit.git")
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