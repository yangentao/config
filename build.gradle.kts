buildscript {
    dependencies {
        classpath("org.jetbrains.dokka:dokka-base:2.0.0")
    }
}
plugins {
    kotlin("jvm") version "2.1.20"
    id("maven-publish")
    signing
    id("org.jetbrains.dokka") version "2.0.0"
}

group = "io.github.yangentao"
version = "1.0.2"
val artifactName = "config"
val githubLib = "config"
val libDesc = "config format, List, Map, String, null."


repositories {
    mavenCentral()
//    maven("https://app800.cn/maven/repository/public/")
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
    jvmToolchain(21)
//    compilerOptions {
//        jvmTarget = JvmTarget.JVM_21
//    }
}
java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
//    withJavadocJar()
//    withSourcesJar()
//    artifact(dokkaJar)
}
//tasks.register<Jar>("dokkaJavadocJar") {
//
//    dependsOn(tasks["dokkaJavadoc"])
//    from(tasks["dokkaJavadoc"].flatMap { it.outputDirectory })
//    archiveClassifier.set("javadoc")
//}
//val javadocJar by tasks.registering(Jar::class) {
//    archiveClassifier.set("javadoc")
//    from(tasks["dokkaHtml"])
//}

//dokka{
//    moduleName.set("Project Name")
//    dokkaPublications.html {
//        suppressInheritedMembers.set(true)
//        failOnWarning.set(true)
//    }
//    dokkaSourceSets.main {
//        includes.from("README.md")
//        sourceLink {
//            localDirectory.set(file("src/main/kotlin"))
//            remoteUrl("https://example.com/src")
//            remoteLineSuffix.set("#L")
//        }
//    }
//    pluginsConfiguration.html {
//        customStyleSheets.from("styles.css")
//        customAssets.from("logo.png")
//        footerMessage.set("(c) yangentao")
//    }
//}

afterEvaluate {
    val sourcesJar = task<Jar>("sourcesJar") {
        from(sourceSets["main"].allSource)
        archiveClassifier.set("sources")
    }

    val dokkaJar = task<Jar>("dokkaJar") {
        from(tasks["dokkaHtml"])
        group = JavaBasePlugin.DOCUMENTATION_GROUP
        archiveClassifier.set("javadoc")
    }
    publishing {
        publications {
            create<MavenPublication>("release") {
                version = project.version.toString()
                groupId = project.group.toString()
                artifactId = artifactName

                from(components["java"])
                artifacts {
                    artifact(sourcesJar)
                    artifact(dokkaJar)
                }

                pom {
                    name = artifactName
                    description = libDesc
                    url = "https://github.com/yangentao/${githubLib}"
                    licenses {
                        license {
                            name = "The Apache License, Version 2.0"
                            url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                        }
                    }
                    developers {
                        developer {
                            id = "yangentao"
                            name = "YangEntao"
                            email = "entaoyang@163.com"
                        }
                    }
                    scm {
                        connection = "scm:git@github.com:yangentao/${githubLib}.git"
                        developerConnection = "scm:git@github.com:yangentao/${githubLib}.git"
                        url = "https://github.com/yangentao/${githubLib}/tree/main"
                    }
                }
            }
        }
        repositories {
            maven {
                name = "App800"
                url = uri("https://app800.cn/maven/repository/public/")
                credentials {
                    username = providers.gradleProperty("ARCHIVA_USERNAME").get()
                    password = providers.gradleProperty("ARCHIVA_PASSWORD").get()
                }
            }
            maven {
                name = "Sonatype"
                url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                credentials {
                    username = providers.gradleProperty("MV_USER").get()
                    password = providers.gradleProperty("MV_PWD").get()
                }
            }
        }
    }
    signing {
        sign(configurations.archives.get())
        sign(publishing.publications["release"])
    }
}


