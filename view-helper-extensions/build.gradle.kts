import com.android.build.gradle.api.LibraryVariant
import org.gradle.api.internal.DefaultDomainObjectSet

plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-android-extensions")
    id("kotlin-kapt")
    `maven-publish`
}

val kotlinVersion = rootProject.extra.get("kotlinVersion") as String
//val enableTest = rootProject.extra.get("enableTests") as (Project) -> Unit
val publish = rootProject.extra.get("publish") as (p: Project, sourceSet: Set<File>, variant: DefaultDomainObjectSet<LibraryVariant>, classPath: FileCollection ) -> Unit


android {
    compileSdkVersion (29)

    defaultConfig {
        minSdkVersion (26)
        targetSdkVersion (29)
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles ("consumer-rules.pro")
    }

    viewBinding {
        isEnabled = true
    }


    buildTypes {
        getByName("release") {

            isMinifyEnabled = false

            proguardFiles (getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    publish(project,android.sourceSets["main"].java.srcDirs,android.libraryVariants, project.files(android.bootClasspath.joinToString(File.pathSeparator)) )

}

dependencies {

    implementation ("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation ("androidx.core:core-ktx:1.2.0")
    implementation ("androidx.appcompat:appcompat:1.1.0")
    implementation ("com.google.android.material:material:1.1.0")
    testImplementation ("junit:junit:4.+")
    androidTestImplementation ("androidx.test.ext:junit:1.1.1")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.2.0")
}