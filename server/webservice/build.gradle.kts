plugins { java }

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.vertx:vertx-web:4.4.0")
    implementation("io.vertx:vertx-auth-jwt:4.5.2")
    runtimeOnly("io.netty:netty-resolver-dns-native-macos:4.1.101.Final:osx-x86_64")
    implementation("com.rabbitmq:amqp-client:5.20.0")
    testImplementation("io.vertx:vertx-junit5:4.5.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.9.2")
    testImplementation(project(":game"))
    testImplementation("org.mongodb:mongodb-driver-sync:4.11.1")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(19))
    }
}

val environmentVariables: Map<String, Any> by rootProject.extra

// Task used to run the microservice related to the web service
task("web-service", JavaExec::class) {
    environment = environmentVariables
    mainClass.set("it.unibo.sd.project.webservice.Main")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.named<Test>("test") {
    environment = environmentVariables
    useJUnitPlatform()
}