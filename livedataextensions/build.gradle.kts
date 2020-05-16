import org.jetbrains.kotlin.cli.jvm.main

plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-android-extensions")
    id("kotlin-kapt")
    `maven-publish`

}

sourceSets {
    main(arrayOf())
}

val kotlinVersion = rootProject.extra.get("kotlinVersion") as String
val enableJavadoc = rootProject.extra.get("enableJavadoc") as (Project, Set<File>) -> Unit
val enableTest = rootProject.extra.get("enableTests") as (Project) -> Unit
val configurePublishing = rootProject.extra.get("configurePublishing") as (Project, Set<File>) -> Unit

val mainSourceSet =  project.android.sourceSets["main"].java.srcDirs
enableJavadoc(project, mainSourceSet)
//enableTest(project)

configurePublishing(project,mainSourceSet)

val versionName = "1.0.6"
android {
    compileSdkVersion(29)
    buildToolsVersion ("29.0.2")

    defaultConfig {
        minSdkVersion (26)
        targetSdkVersion (29)
        versionCode = 1
        versionName = versionName

        testInstrumentationRunner  = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles ("consumer-rules.pro")
    }

    buildTypes {

        getByName("release") {
            isMinifyEnabled = false
            isUseProguard = false
            //proguardFiles (getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    afterEvaluate {
        publishing {
            publications {
                create<MavenPublication>("release") {
                    from(components["release"])
                    groupId = "com.zelgius.android-libraries"
                    //artifactId = "livedataextensions-release"
                    version = versionName
                }
            }

            repositories {
                maven("${project.rootDir}/releases")
            }


        }
    }
}

dependencies {
    implementation (fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
    implementation ("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlinVersion")
    // implementation ("androidx.core:core-ktx:1.2.0")
    implementation("androidx.lifecycle:lifecycle-livedata:2.2.0")
    testImplementation ("junit:junit:4.13")
    androidTestImplementation("androidx.test.ext:junit:1.1.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.1.0")
}
