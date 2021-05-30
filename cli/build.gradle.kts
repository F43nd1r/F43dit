plugins {
    kotlin
    application
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("com.github.breadmoirai.github-release") version "2.2.12"
}

application {
    applicationName = "F43dit"
    mainClass.set("com.faendir.om.cli.MainKt")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.2")
    implementation(project(":dsl"))
}

githubRelease {
    token(project.findProperty("githubToken") as? String ?: System.getenv("GITHUB_TOKEN"))
    owner("F43nd1r")
    repo("F43dit")
    releaseAssets(tasks.getByName("shadowDistTar").outputs, tasks.getByName("shadowDistZip").outputs)
}

tasks.withType<AbstractArchiveTask> {
    archiveBaseName.set("F43dit")
}

tasks.register("publish") {
    group = "publishing"
    dependsOn("githubRelease")
}
