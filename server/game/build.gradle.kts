plugins {
    java
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.mongodb:mongodb-driver-sync:4.11.1")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.rabbitmq:amqp-client:5.20.0")
    implementation("at.favre.lib:bcrypt:0.10.2")
    implementation("org.slf4j:slf4j-log4j12:2.0.9")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.9.2")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(19))
    }
}

// Task used to run the microservice related to the game
task("game", JavaExec::class) {
    val env = mapOf (
            "RABBIT_HOST" to "localhost",
            "MONGO_HOST" to "mongodb://localhost:27017"
        )
    environment = env
    mainClass.set("it.unibo.sd.project.mastermind.Main")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
