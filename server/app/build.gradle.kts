plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    java
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.9.2")
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(19))
    }
}

// Task used to run the microservice related to the game
/*task("game", JavaExec::class) {
    mainClass.set("it.unibo.sd.project.mastermind.Main")
    classpath = sourceSets["main"].runtimeClasspath
}*/

// Task used to run the microservice related to the web service
/*task("web-service", JavaExec::class) {
    mainClass.set("it.unibo.sd.project.webservice.Server")
    classpath = sourceSets["main"].runtimeClasspath
}*/

tasks.named<Test>("test") {
    useJUnitPlatform()
}
