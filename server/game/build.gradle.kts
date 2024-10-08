plugins {
    java
    jacoco
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
    implementation("io.vertx:vertx-auth-jwt:4.5.2")
    runtimeOnly("io.netty:netty-resolver-dns-native-macos:4.1.101.Final:osx-x86_64")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.9.2")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

val environmentVariables: Map<String, Any> by rootProject.extra

// Task used to run the microservice related to the game
task("game", JavaExec::class) {
    environment = environmentVariables
    mainClass.set("it.unibo.sd.project.mastermind.Main")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.named<Test>("test") {
    environment = environmentVariables
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}

jacoco {
    toolVersion = "0.8.12"
    reportsDirectory = layout.buildDirectory.dir("jacocoReports")
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
    reports {
        xml.required = false
        csv.required = false
        html.outputLocation = layout.buildDirectory.dir("jacocoHtml")
    }
}
