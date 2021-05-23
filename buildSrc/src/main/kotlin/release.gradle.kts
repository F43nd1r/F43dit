plugins {
    id("repositories")
    id("org.jetbrains.dokka")
    id("io.github.gradle-nexus.publish-plugin")
}

tasks.register("build") {
    group = "build"
}

tasks.register("publish") {
    group = "publishing"
    subprojects {
        tasks.findByName("publish")?.let { dependsOn(it) }
        tasks.findByName("publishToSonatype")?.let { dependsOn(it) }
    }
    dependsOn("closeAndReleaseSonatypeStagingRepository")
}

tasks.register<Delete>("clean") {
    group = "build"
    delete = setOf(buildDir)
}

nexusPublishing {
    repositories {
        sonatype {
            username.set(project.findProperty("ossrhUser") as? String ?: System.getenv("OSSRH_USER"))
            password.set(project.findProperty("ossrhPassword") as? String ?: System.getenv("OSSRH_PASSWORD"))
        }
    }
}