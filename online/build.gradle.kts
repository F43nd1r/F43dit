import org.springframework.boot.gradle.tasks.bundling.BootJar

@Suppress("DSL_SCOPE_VIOLATION") // TODO remove when https://youtrack.jetbrains.com/issue/KTIJ-19369 is fixed
plugins {
    kotlin
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependencyManagement)
    alias(libs.plugins.gradle.docker)
    alias(libs.plugins.vaadin)
}

vaadin {
    pnpmEnable = true
    productionMode = true
}

dependencyManagement {
    imports {
        mavenBom(libs.vaadin.bom.get().toString())
    }
}

dependencies {
    implementation("com.vaadin:vaadin-spring-boot-starter") {
        listOf(
            "com.vaadin.webjar",
            "org.webjars.bowergithub.insites",
            "org.webjars.bowergithub.polymer",
            "org.webjars.bowergithub.polymerelements",
            "org.webjars.bowergithub.vaadin",
            "org.webjars.bowergithub.webcomponents"
        ).forEach { group -> exclude(group = group) }
    }
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    implementation(projects.dsl)
    implementation(libs.ace)
    implementation(libs.classgraph)
    implementation(libs.kotlinx.coroutines)
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

docker {
    name = "f43nd1r/omsek"
    tag("latest", "docker.io/f43nd1r/omsekt")
    dependsOn(tasks.findByName("bootJar"))
    copySpec.into(".") {
        into("build/libs") {
            from(tasks.getByName<BootJar>("bootJar").outputs)
        }
        from("java.policy")
    }
}

tasks.register("publish") {
    group = "publishing"
    dependsOn("dockerPush")
}
