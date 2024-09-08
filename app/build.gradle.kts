plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)

    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)

    id("com.google.gms.google-services")
    id("kotlin-parcelize")

    id("com.google.android.gms.oss-licenses-plugin")
}

android {
    namespace = "com.team5.alarmmemo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.team5.alarmmemo"
        minSdk = 26
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures{
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("androidx.fragment:fragment-ktx:1.8.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    implementation ("androidx.security:security-crypto:1.1.0-alpha03")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("androidx.lifecycle:lifecycle-service:2.8.4")
    implementation("com.github.skydoves:colorpickerview:2.3.0")

    implementation("com.google.android.gms:play-services-oss-licenses:17.1.0")

    implementation("com.naver.maps:map-sdk:3.19.1")

    implementation(libs.bundles.retrofit)

    //hilt
    implementation(libs.hilt)
    ksp(libs.hilt.compiler)

    //room
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)

    //firebase
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")

    // kakao
    implementation ("com.kakao.sdk:v2-all:2.20.5")
    implementation ("com.kakao.sdk:v2-user:2.20.5")

    // naver
//    implementation("com.navercorp.nid:oauth:5.10.0") // jdk 11
    implementation("com.navercorp.nid:oauth-jdk8:5.10.0") // jdk

    implementation ("org.jetbrains.kotlin:kotlin-stdlib:1.9.24")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation ("androidx.legacy:legacy-support-core-utils:1.0.0")
    implementation ("androidx.browser:browser:1.8.0")
//    implementation ("androidx.core:core-ktx:1.13.1")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.moshi:moshi-kotlin:1.11.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation ("com.airbnb.android:lottie:6.5.0")

    // google
//    implementation ("com.google.gms:google-services:4.3.15")
    implementation ("com.google.firebase:firebase-auth:22.0.0")
    implementation ("com.google.android.gms:play-services-auth:20.5.0")
    implementation ("com.google.maps.android:android-maps-utils:2.2.4")

}