@Suppress("DSL_SCOPE_VIOLATION") // TODO remove when https://youtrack.jetbrains.com/issue/KTIJ-19369 is fixed
plugins {
    kotlin
    application
    alias(libs.plugins.shadow)
    alias(libs.plugins.githubRelease)
}

application {
    applicationName = "F43dit"
    mainClass.set("com.faendir.om.cli.MainKt")
}

dependencies {
    implementation(libs.kotlinx.cli)
    implementation(projects.dsl)
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
