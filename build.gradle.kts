import com.android.build.gradle.api.LibraryVariant
import java.io.FileInputStream
import java.util.Properties
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.dokka.gradle.DokkaTask
import org.gradle.api.internal.DefaultDomainObjectSet

plugins {
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
    val kotlinVersion by extra("1.4.0-rc")

    repositories {
        google()
        jcenter()
        maven(url = "https://dl.bintray.com/poldz123/maven/")
        maven(url = "https://dl.bintray.com/kotlin/kotlin-dev")
        maven ( "https://dl.bintray.com/kotlin/kotlin-eap/" )

    }

    dependencies {
        classpath("com.android.tools.build:gradle:4.2.0-alpha07")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${kotlinVersion}")
        classpath("com.google.gms:google-services:4.3.3")
        classpath("com.google.firebase:firebase-plugins:2.0.0")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle.kts.kts files
    }

    allprojects {
        repositories {
            google()
            jcenter()
            maven(url = "https://dl.bintray.com/kotlin/dokka")
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


tasks.register("commit", Exec::class) {
    //dependsOn(getTasksByName("dokkaDoc", true))
    dependsOn(getTasksByName("done", true))

    //doLast {
        val builder = StringBuilder()
        val status = "git status".runCommand()
        subprojects.forEach {
            with(File(it.projectDir, "version.properties")) {

                if(exists() && status.contains("${it.name}/$name")) {
                    println("${it.name} was modified")
                    builder.append(if(builder.isEmpty())"${it.name}: " else " - ${it.name}: " )

                    it.getProperty<String>("commit_message", "version.properties")?.let { s ->
                        builder.append(s )
                    }?: it.getProperty<String>("version", "version.properties")?.let { s ->
                        builder.append(s)
                    }
                }
            }
        }

        println("commit message: $builder")

        /*println("git commit --all -m \"$builder\"".runCommand(failOnError = true))
        println("git push".runCommand(failOnError = true))*/
    //}

    if(builder.isNotEmpty()) {
        //exec {
        workingDir("./")
        commandLine("./script_git.sh", "$builder")
        //}
    }
}

val publish by extra {
    { p: Project, sourceSet: Set<File>, variant: DefaultDomainObjectSet<LibraryVariant>, classPath: FileCollection ->

        val buildDoc = p.tasks.create("buildDoc",Javadoc::class) {
            isFailOnError = false
            source ( sourceSet)
            classpath += project.files(classPath)
            variant.forEach { variant ->
                if (variant.name == "release") {
                    classpath += variant.javaCompileProvider.get().classpath
                }
            }
            exclude ("**/R.html", "**/R.*.html", "**/index.html")
        }

        val doc = p.tasks.create("doc", Jar::class) {
            dependsOn(buildDoc)
            archiveClassifier.set("javadoc")
            from(buildDoc.destinationDir)
        }

        val source = p.tasks.create("androidSources", Jar::class) {
            archiveClassifier.set("sources")
            from (sourceSet)
        }

        p.afterEvaluate {
            p.publishing {
                publications {
                    create<MavenPublication>("release") {
                        groupId = "com.zelgius.android-libraries"
                        //artifactId = "livedataextensions-release"
                        version =  p.getProperty<String>("version", "version.properties")

                        from(p.components["release"])

                        artifacts {
                            artifact(doc)
                            artifact(source)
                        }
                        pom {
                            withXml {
                                asNode()
                                    .appendNode("build")
                                    .appendNode("plugins")
                                    .appendNode("plugin").apply {
                                        appendNode("groupId", "org.apache.maven.plugins")
                                        appendNode("artifactId", "maven-javadoc-plugin")
                                        appendNode("version", version)
                                        appendNode("configuration").apply {
                                            //appendNode("show", "private")
                                            //appendNode("nohelp", "true")
                                        }
                                    }
                            }
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

fun String.runCommand(workingDir: File = file("./"), failOnError: Boolean = false): String {
    val parts = this.split("\\s".toRegex())
    val proc = ProcessBuilder(*parts.toTypedArray())
        .directory(workingDir)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()

    println(this)
    proc.waitFor(1, TimeUnit.MINUTES)

    val error = proc.errorStream.bufferedReader().readText().trim()
    if(error.isNotBlank()) {
        if(failOnError)
            error("$this\n$error")
        else
            logger.error(error)
    }

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