import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    kotlin
    id("org.springframework.boot") version "2.5.0"
    id("com.vaadin")
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("com.palantir.docker") version "0.26.0"
}

version = "1.2.0"

vaadin {
    pnpmEnable = true
    productionMode = true
}

dependencyManagement {
    imports {
        mavenBom("com.vaadin:vaadin-bom:${project.properties["vaadinVersion"]}")
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
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.71")
    implementation(project(":dsl"))
    implementation("de.f0rce:ace:1.2.1")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

docker {
    name = "f43nd1r/omsekt:latest"
    dependsOn(tasks.findByName("bootJar"))
    copySpec.into(".") {
        into("build/libs") {
            from(tasks.getByName<BootJar>("bootJar").outputs)
        }
        from("java.policy")
    }
}
