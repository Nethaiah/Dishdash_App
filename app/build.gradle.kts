plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.dishdash"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.dishdash"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    //implementation(libs.health.services.client)
    implementation(libs.shimmer)
    implementation(libs.gms.auth)
    implementation(libs.retroFit)
    implementation(libs.gson)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    annotationProcessor(libs.glide.compiler)
    implementation(libs.glide)
    implementation(libs.bcrypt)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}