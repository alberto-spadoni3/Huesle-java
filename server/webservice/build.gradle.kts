plugins { java }

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.vertx:vertx-web:4.4.0")
    runtimeOnly("io.netty:netty-resolver-dns-native-macos:4.1.101.Final:osx-x86_64")
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
task("web-service", JavaExec::class) {
    environment = mapOf("RABBIT_HOST" to "localhost")
    mainClass.set("it.unibo.sd.project.webservice.Main")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}