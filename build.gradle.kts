import org.jetbrains.kotlin.backend.common.push
import java.net.URI
import java.io.FileInputStream
import java.util.Properties

plugins {
    id("java")
    `maven-publish`
}
val componentJava by extra { components["java"]!! }

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
        classpath("com.android.tools.build:gradle:4.1.0-alpha09")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${kotlinVersion}")
        classpath("com.google.gms:google-services:4.3.3")
        classpath("com.google.firebase:firebase-plugins:2.0.0")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }

}

allprojects {
    repositories {
        google()
        jcenter()

    }

    if(this.name != this.rootProject.name) { //Nothing is in the root project
        afterEvaluate {
            publishing {
                publications {
                    create<MavenPublication>("release") {
                        groupId = "com.zelgius.${this@allprojects.name}"
                        //artifactId = "livedataextensions-release"
                        version = "1.0"

                        //from(components["java"])
                        artifacts {
                            add("archives", tasks["sourceJar"])
                            add("archives", tasks["testJar"])
                            add("archives", tasks["javadocJar"])
                        }
                    }
                }

                repositories {
                    maven("${project.rootDir}/releases")
                }
            }
        }
    }

}

val getProps by extra {
    fun(propName: String): Any {
        val propsFile = rootProject.file("local.properties")
        return if (propsFile.exists()) {
            val props = Properties()
            props.load(FileInputStream(propsFile))
            props[propName] as Any
        } else {
            ""
        }
    }
}

createRequiredPublishingTasks(project)


val enableJavadoc by extra {
    { p: Project ->
        p.tasks.create("javadoc", Javadoc::class) {
            source = sourceSets["main"].allJava

        }
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

fun createRequiredPublishingTasks(p: Project) {
    p.tasks.create("sourceJar", Jar::class) {

        dependsOn(JavaPlugin.CLASSES_TASK_NAME)
        archiveClassifier.set("sources")
        println(p.sourceSets)
        from(sourceSets["main"].allSource)
    }

    p.tasks.create("javadocJar", Jar::class) {
        try {
            dependsOn(JavaPlugin.JAVADOC_TASK_NAME)
            archiveClassifier.set("javadoc")
            from(p.tasks["javadoc"])
            println("javadoc compiled")
        } catch (e: Exception) {
            logger.log(LogLevel.ERROR, "Cannot build javadoc: ${e.message}")
        }

        p.tasks.create("testJar", Jar::class) {
            try {
                dependsOn(JavaPlugin.JAVADOC_TASK_NAME)
                archiveClassifier.set("tests")
                from(p.tasks["tests"])
                println("tests compiled")

            } catch (e: Exception) {
                logger.log(LogLevel.ERROR, "Cannot build test: ${e.message}")
            }
        }
    }
}

val configurePublishing by extra {
    { p: Project ->
        p.apply(plugin = "maven-publish")

        createRequiredPublishingTasks(p)
    }
}
