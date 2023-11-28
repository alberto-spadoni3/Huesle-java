plugins {
    java
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.mongodb:mongodb-driver-sync:4.11.1")
    implementation("com.google.code.gson:gson:2.10.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.9.2")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(19))
    }
}

// Task used to run the microservice related to the game
/*task("game", JavaExec::class) {
    mainClass.set("it.unibo.sd.project.mastermind.Game")
    classpath = sourceSets["main"].runtimeClasspath
}*/

tasks.named<Test>("test") {
    useJUnitPlatform()
}
