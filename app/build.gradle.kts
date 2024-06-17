import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.jetbrains.kotlin.parcelize)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.daggerHilt)
    id("kotlin-kapt")
}

android {
    namespace = "com.thesis.dishdetective_xml"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.thesis.dishdetective_xml"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // Set value part
        val properties = Properties()
        properties.load(FileInputStream(project.rootProject.file("local.properties")))
        val supabaseAnonKey = properties.getProperty("SUPABASE_ANON_KEY")
        val secret = properties.getProperty("SECRET")
        val supabaseUrl = properties.getProperty("SUPABASE_URL")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseAnonKey\"")
        buildConfigField("String", "SECRET", "\"$secret\"")
        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
    }

    buildTypes {
        release {
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
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)

    implementation(libs.tensorflowLite)
    implementation(libs.tensorflowLiteSupport)
    implementation(libs.tensorflowLiteGpuDelegatePlugin)
    implementation(libs.tensorflowLiteGpuApi)
    implementation(libs.tensorflowLiteApi)
    implementation(libs.tensorflowLiteGpu)
    implementation(libs.androidx.runtime.livedata)

    val supabase = "2.5.0"
    implementation(platform("io.github.jan-tennert.supabase:bom:${supabase}"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt:${supabase}")
    implementation("io.github.jan-tennert.supabase:storage-kt:${supabase}")
    implementation("io.github.jan-tennert.supabase:gotrue-kt:${supabase}")

    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.utils)

    implementation(libs.androidx.annotation)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    implementation(libs.dagger.hilt)
    kapt(libs.dagger.hilt.compiler)
    kapt(libs.dagger.hilt.androidx)
    implementation(libs.dagger.hilt.navigation.compose)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

}