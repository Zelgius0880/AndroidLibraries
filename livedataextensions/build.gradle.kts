import org.jetbrains.kotlin.cli.jvm.main
import java.lang.IllegalStateException

plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-android-extensions")
    id("kotlin-kapt")
}

sourceSets {
    main(arrayOf())
}

val kotlinVersion = rootProject.extra.get("kotlinVersion") as String
val enableJavadoc = rootProject.extra.get("enableJavadoc") as (Project, FileTree) -> Unit
val enableTest = rootProject.extra.get("enableTests") as (Project) -> Unit
val configurePublishing = rootProject.extra.get("configurePublishing") as (Project, FileTree) -> Unit

val mainSourceSet =  project.android.sourceSets["main"].java.getSourceFiles()
enableJavadoc(project, mainSourceSet)
enableTest(project)

configurePublishing(project,mainSourceSet)

android {
    compileSdkVersion(29)
    buildToolsVersion ("29.0.2")

    defaultConfig {
        minSdkVersion (26)
        targetSdkVersion (29)
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner  = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles ("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles (getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

}

dependencies {
    implementation (fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation ("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlinVersion")
    // implementation ("androidx.core:core-ktx:1.2.0")
    implementation("androidx.lifecycle:lifecycle-livedata:2.2.0")
    testImplementation ("junit:junit:4.13")
    androidTestImplementation("androidx.test.ext:junit:1.1.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.1.0")
}
