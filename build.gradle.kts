// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false

    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false

    id("com.google.gms.google-services") version "4.4.2" apply false
}

buildscript{
    dependencies{
        classpath("com.google.android.gms:oss-licenses-plugin:0.10.6")
        classpath("com.google.gms:google-services:4.3.10")
    }
}