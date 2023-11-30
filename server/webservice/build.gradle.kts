plugins {
    java
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.rabbitmq:amqp-client:5.20.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.9.2")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(19))
    }
}

// Task used to run the microservice related to the web service
/*task("web-service", JavaExec::class) {
    mainClass.set("it.unibo.sd.project.webservice.WebServer")
    classpath = sourceSets["main"].runtimeClasspath
}*/

tasks.named<Test>("test") {
    useJUnitPlatform()
}