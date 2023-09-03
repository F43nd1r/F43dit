import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    kotlin
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.gradle.docker)
    alias(libs.plugins.vaadin)
}

vaadin {
    pnpmEnable = true
    productionMode = true
}

dependencies {
    implementation(libs.orgSpringframeworkBoot.springBootStarterWeb)
    implementation(libs.comVaadin.vaadinSpringBootStarter){
        listOf(
            "com.vaadin.webjar",
            "org.webjars.bowergithub.insites",
            "org.webjars.bowergithub.polymer",
            "org.webjars.bowergithub.polymerelements",
            "org.webjars.bowergithub.vaadin",
            "org.webjars.bowergithub.webcomponents"
        ).forEach { group -> exclude(group = group) }
    }
    developmentOnly(libs.orgSpringframeworkBoot.springBootDevtools)
    implementation(projects.dsl)
    implementation(libs.ace)
    implementation(libs.classgraph)
    implementation(libs.kotlinx.coroutines)
}

docker {
    name = "f43nd1r/omsekt"
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
