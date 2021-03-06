import com.android.build.gradle.api.LibraryVariant
import org.jetbrains.kotlin.cli.jvm.main
import org.gradle.api.internal.DefaultDomainObjectSet


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
val enableTest = rootProject.extra.get("enableTests") as (Project) -> Unit
val publish = rootProject.extra.get("publish") as (p: Project, sourceSet: Set<File>, variant: DefaultDomainObjectSet<LibraryVariant>, classPath: FileCollection ) -> Unit

//enableTest(project)


val versionName = "1.0.6"
android {
    compileSdkVersion(29)
    buildToolsVersion("29.0.2")

    defaultConfig {
        minSdkVersion(26)
        targetSdkVersion(29)
        versionCode = 1
        versionName = versionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    configurations.all {
        resolutionStrategy {
            force("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
        }
    }


    buildTypes {

        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    publish(project,android.sourceSets["main"].java.srcDirs,android.libraryVariants, project.files(android.bootClasspath.joinToString(File.pathSeparator)) )

}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    // implementation ("androidx.core:core-ktx:1.2.0")
    implementation("androidx.lifecycle:lifecycle-livedata:2.2.0")
    testImplementation("junit:junit:4.13")
    androidTestImplementation("androidx.test.ext:junit:1.1.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
}
