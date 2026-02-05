plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
    id("kotlin-android")
    id("kotlin-kapt")
    id("android-module-dependencies")
    id("test-module-dependencies")
    id("jacoco-module-dependencies")
}

android {
    namespace = "app.aaps.pump.medtrum"
    buildFeatures {
        dataBinding = true
    }
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:interfaces"))
    implementation(project(":core:keys"))
    implementation(project(":core:libraries"))
    implementation(project(":core:objects"))
    implementation(project(":core:ui"))
    implementation(project(":core:utils"))
    implementation(project(":core:validators"))
    implementation(project(":pump:common"))

    testImplementation(project(":shared:tests"))
    testImplementation(project(":core:objects"))

    kapt(libs.com.google.dagger.compiler)
    kapt(libs.com.google.dagger.android.processor)
    // Workaround for Kotlin 2.3.0: Dagger bundles older kotlin-metadata-jvm that doesn't support 2.3.0 metadata
    kapt(libs.kotlin.metadata.jvm)
}
