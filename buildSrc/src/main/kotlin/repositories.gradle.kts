repositories {
    mavenCentral()
    google()
    maven { setUrl("https://jitpack.io") }
    //TODO: Remove when kotlinx.html is available in maven central
    maven { setUrl("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven") }
    maven { setUrl("https://maven.vaadin.com/vaadin-prereleases/")}
    maven { setUrl("https://maven.vaadin.com/vaadin-addons/")}
    jcenter()
    mavenLocal()
}