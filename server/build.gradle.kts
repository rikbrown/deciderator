plugins {
    java
    kotlin("jvm") version "1.4-M1"
}

group = "codes.rik"
version = "3.0"

repositories {
  mavenCentral()
  maven("https://dl.bintray.com/kotlin/kotlin-eap")
  maven("https://kotlin.bintray.com/kotlinx")
  maven("https://jitpack.io")
}

dependencies {
  implementation(kotlin("stdlib-jdk8"))
  implementation(kotlin("reflect"))
  implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-jdk8", "1.3.5")

  implementation("org.springframework.boot", "spring-boot-starter-websocket", "2.2.6.RELEASE")
  implementation("com.fasterxml.jackson.module", "jackson-module-kotlin", "2.10.+")
  implementation("com.fasterxml.jackson.datatype", "jackson-datatype-jdk8", "2.10.+")
  implementation("com.fasterxml.jackson.datatype", "jackson-datatype-jsr310", "2.10.+")
  implementation("org.apache.commons", "commons-lang3", "3.10")
  implementation("com.github.ntrrgc", "ts-generator", "1.1.1")
  implementation("io.reactivex.rxjava3", "rxkotlin", "3.0.0")


  testImplementation("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_12
}
tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "12"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "12"
    }
}
