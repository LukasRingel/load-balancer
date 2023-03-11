plugins {
  kotlin("jvm") version "1.8.0"
}

group = "de.lukasringel"
version = "1.0-SNAPSHOT"

dependencies {
  // connections
  implementation("io.netty:netty-all:5.0.0.Alpha2")

  // console
  implementation("org.jline:jline:3.22.0")

  // sources
  implementation("com.sparkjava:spark-core:2.9.4")
}

kotlin {
  jvmToolchain(17)
}