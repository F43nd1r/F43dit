import com.faendir.gradle.createWithBomSupport

plugins {
    id("com.faendir.gradle.bom-version-catalog") version "1.5.0"
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
rootProject.name = "F43dit"
rootDir.listFiles()?.forEach {
    if(it.isDirectory && it.name != "buildSrc" && it.list()?.contains("build.gradle.kts") == true) {
        include(it.name)
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
        maven { setUrl("https://jitpack.io") }
        maven { setUrl("https://maven.vaadin.com/vaadin-prereleases/")}
        maven { setUrl("https://maven.vaadin.com/vaadin-addons/")}
        maven { setUrl("https://oss.sonatype.org/content/repositories/snapshots")}
        mavenLocal()
    }
    versionCatalogs {
        createWithBomSupport("libs") {
            fromBomAlias("spring-boot-bom")
            fromBomAlias("vaadin-bom")
        }
    }
}