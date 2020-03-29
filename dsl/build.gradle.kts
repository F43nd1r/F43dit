plugins {
    kotlin("jvm")
    `maven-publish`
}

group = "com.faendir.om"
version = "1.1.3"

repositories {
    jcenter()
    maven { setUrl("https://dl.bintray.com/f43nd1r/maven") }
}

dependencies {
    api("com.faendir.om:omsp:1.3.4")
    implementation("com.faendir.om:scriptdef:1.0.2")
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

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.getByName("main").allSource)
}

publishing {
    repositories {
        mavenLocal()
        maven {
            setUrl("https://api.bintray.com/maven/f43nd1r/maven/om-dsl/;publish=1")
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
            artifact(sourcesJar)
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