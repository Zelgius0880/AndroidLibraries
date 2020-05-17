plugins {
    java
    kotlin("jvm") version "1.3.72"
    `maven-publish`
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}
publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.zelgius.android-libraries"
            artifactId = "livedataextensions"
            version = "1.1"

            from(components["java"])
        }
    }

    repositories {
        maven("${project.rootDir}/releases")
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    testImplementation("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}
