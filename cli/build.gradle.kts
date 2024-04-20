import com.github.breadmoirai.githubreleaseplugin.GithubReleaseTask

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
    owner.set("F43nd1r")
    repo.set("F43dit")
    releaseAssets(tasks.shadowDistTar, tasks.shadowDistZip)
    generateReleaseNotes.set(true)
    targetCommitish.set("master")
}

tasks.withType<GithubReleaseTask> {
    dependsOn(tasks.shadowDistTar, tasks.shadowDistZip, tasks.distTar, tasks.distZip)
}

tasks.withType<AbstractArchiveTask> {
    archiveBaseName.set("F43dit")
}

tasks.register("publish") {
    group = "publishing"
    dependsOn(tasks.githubRelease)
}
