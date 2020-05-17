import java.io.FileInputStream
import java.util.Properties
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.dokka.gradle.DokkaTask
import org.gradle.api.internal.tasks.TaskDependencyResolveException

plugins {
    kotlin("jvm") version "1.3.72"
    `maven-publish`
    id("org.jetbrains.dokka") version "0.10.1"
}

//val componentJava by extra { components["java"]!! }

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    val kotlinVersion by extra { "1.3.72" }

    repositories {
        google()
        jcenter()
        maven(url = "https://dl.bintray.com/poldz123/maven/")
        maven(url = "https://dl.bintray.com/kotlin/kotlin-dev")
    }

    dependencies {
        classpath("com.android.tools.build:gradle:3.6.3")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${kotlinVersion}")
        classpath("com.google.gms:google-services:4.3.3")
        classpath("com.google.firebase:firebase-plugins:2.0.0")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }

    allprojects {
        repositories {
            google()
            jcenter()
            maven(url = "https://dl.bintray.com/kotlin/dokka")
        }
    }


}

allprojects {
    if (this.name != this.rootProject.name) { //Nothing is in the root project
        val dokka = tasks.create("dokkaDoc", DokkaTask::class) {
            outputFormat = "html"
            outputDirectory = "${buildDir}/dokka"
            subProjects = subprojects.map { it.name }
        }

        val doc = tasks.create("dokkaJar", Jar::class) {
            group = JavaBasePlugin.DOCUMENTATION_GROUP
            description = "Assembles Kotlin docs with Dokka"
            archiveClassifier.set("javadoc")
            from(dokka)
            dependsOn(dokka)
        }

        afterEvaluate {

            publishing {
                publications {
                    create<MavenPublication>("release") {
                        groupId = "com.zelgius.android-libraries"
                        //artifactId = "livedataextensions-release"
                        version =
                            getProperty("version", "deploy.properties") ?: "0.0"

                        //from(components["java"])
                        //from(projectComponents[this@allprojects])

                        artifact(doc)
                        artifact("${this@allprojects.buildDir}/outputs/aar/${this@allprojects.name}-release.aar")

                    }
                }

                repositories {
                    maven("${project.rootDir}/releases")
                }
            }
        }
    }
}

tasks.register("done") {
    //dependsOn(getTasksByName("dokkaDoc", true))
    dependsOn(getTasksByName("assemble", true))
    dependsOn(getTasksByName("publish", true))

    doLast {
        "git add ${project.rootDir}/releases".runCommand()
        println("done")
    }
}

val enableTests by extra {
    { p: Project ->

        p.tasks.create("tests", Test::class) {
// enable TestNG support (default is JUnit)
            useTestNG()
// enable JUnit Platform (a.k.a. JUnit 5) support
            useJUnitPlatform()

// set a system property for the test JVM(s)
//systemProperty ("some.prop", "value")

// explicitly include or exclude tests
//include("org/foo/**")
//exclude("org/boo/**")

// show standard out and standard error of the test JVM(s) on the console
            testLogging.showStandardStreams = true

// set heap size for the test JVM(s)
            minHeapSize = "128m"
            maxHeapSize = "512m"

// set JVM arguments for the test JVM(s)
            jvmArgs = listOf("-XX:MaxPermSize=256m")

// listen to events in the test execution lifecycle
            addTestListener(object : TestListener {
                override fun beforeTest(testDescriptor: TestDescriptor?) {
                    logger.lifecycle("Running test: $testDescriptor")
                }

                override fun afterSuite(suite: TestDescriptor?, result: TestResult?) {
                }

                override fun beforeSuite(suite: TestDescriptor?) {
                }

                override fun afterTest(testDescriptor: TestDescriptor?, result: TestResult?) {
                }

            })

// Fail the "test" task on the first test failure
            failFast = true

// listen to standard out and standard error of the test JVM(s)
            addTestOutputListener { testDescriptor, outputEvent ->
                logger.lifecycle("Test: $testDescriptor produced standard out/err: $outputEvent.message")
            }
        }
    }
}

fun String.runCommand(workingDir: File = file("./")): String {
    val parts = this.split("\\s".toRegex())
    val proc = ProcessBuilder(*parts.toTypedArray())
        .directory(workingDir)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()

    println(this)
    proc.waitFor(1, TimeUnit.MINUTES)
    return proc.inputStream.bufferedReader().readText().trim()
}

fun <T> Project.getProperty(key: String, fileName: String = "local.properties"): T? {
    val propsFile = file(fileName)
    return if (propsFile.exists()) {
        val props = Properties()
        props.load(FileInputStream(propsFile))
        props[key] as T
    } else {
        null
    }
}