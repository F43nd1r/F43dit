plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
    maven { setUrl("https://maven.vaadin.com/vaadin-prereleases/")}
}

dependencies {
    val kotlinVersion: String by project
    implementation(kotlin("gradle-plugin:$kotlinVersion"))
    val dokkaVersion: String by project
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:$dokkaVersion")
    implementation("org.jetbrains.dokka:dokka-core:$dokkaVersion")
    val nexusPublishVersion: String by project
    implementation("io.github.gradle-nexus:publish-plugin:$nexusPublishVersion")
    val vaadinPluginVersion: String by project
    implementation("com.vaadin:vaadin-gradle-plugin:$vaadinPluginVersion")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}
