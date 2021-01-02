plugins {
  java
  kotlin("jvm") version "1.4.21"
  kotlin("kapt") version "1.4.21"
  id("com.github.johnrengelman.shadow") version "5.1.0"
  id("com.palantir.docker") version "0.25.0"
  id("io.gitlab.arturbosch.detekt") version "1.1.1"
  id("org.jlleitschuh.gradle.ktlint") version "9.0.0"
}

group = "codes.rik"
version = "3.0"
val dockerImageVersion = "0.1"
val dockerImageName = "487129032168.dkr.ecr.us-west-2.amazonaws.com/deciderator-server:$dockerImageVersion"

repositories {
  mavenCentral()
  maven("https://dl.bintray.com/kotlin/kotlin-eap")
  maven("https://kotlin.bintray.com/kotlinx")
  maven("https://jitpack.io")
}

dependencies {
  // kotlin
  implementation(kotlin("stdlib-jdk8"))
  implementation(kotlin("reflect"))
  implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-jdk8", "1.4.2")

  // kapt
  kapt("com.google.dagger", "dagger-compiler", "2.27")

  // 3p
  implementation("org.springframework.boot", "spring-boot-starter-websocket", "2.2.6.RELEASE")
  implementation("com.fasterxml.jackson.module", "jackson-module-kotlin", "2.10.+")
  implementation("com.fasterxml.jackson.datatype", "jackson-datatype-jdk8", "2.10.+")
  implementation("com.fasterxml.jackson.datatype", "jackson-datatype-jsr310", "2.10.+")
  implementation("org.apache.commons", "commons-lang3", "3.10")
  implementation("com.github.ntrrgc", "ts-generator", "1.1.1")
  implementation("io.reactivex.rxjava3", "rxkotlin", "3.0.0")
  implementation("com.google.dagger", "dagger", "2.27")
  implementation("io.github.microutils", "kotlin-logging", "1.7.9")
  implementation("org.slf4j", "slf4j-api", "1.7.30")
//  implementation("org.slf4j", "slf4j-simple", "1.7.30")


  testImplementation("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_13
}
tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "13"
        kotlinOptions.freeCompilerArgs = listOf("-Xopt-in=kotlin.contracts.ExperimentalContracts")
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "13"
    }
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
  manifest {
    attributes(mapOf(
      "Main-Class" to "codes.rik.deciderator.cmd.ApplicationKt",
      "Multi-Release" to "true" // https://stackoverflow.com/questions/53049346/is-log4j2-compatible-with-java-11
    ))
  }

  mergeServiceFiles()
}

docker {
  name = dockerImageName
  setDockerfile(file("docker/Dockerfile"))
  files(tasks["shadowJar"].outputs, "docker/run.sh")
}
