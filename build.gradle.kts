plugins {
    kotlin("jvm") version "2.1.20"
    id("maven-publish")
    id("signing")
    id("org.jetbrains.dokka") version "2.0.0"
}

group = "io.github.yangentao"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
//    implementation(kotlin("reflect"))
    implementation(kotlin("stdlib"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}