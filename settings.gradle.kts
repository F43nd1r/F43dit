enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "F43dit"
rootDir.listFiles()?.forEach {
    if(it.isDirectory && it.name != "buildSrc" && it.list()?.contains("build.gradle.kts") == true) {
        include(it.name)
    }
}
