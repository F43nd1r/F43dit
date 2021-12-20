enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "zachtronics-leaderboard-bot"
rootDir.listFiles()?.forEach {
    if(it.isDirectory && it.name != "buildSrc" && it.list()?.contains("build.gradle.kts") == true) {
        include(it.name)
    }
}
