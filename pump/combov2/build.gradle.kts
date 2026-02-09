plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    id("com.android.legacy-kapt") version libs.versions.gradlePlugin.get()
    id("android-module-dependencies")
    id("test-module-dependencies")
    id("jacoco-module-dependencies")
}

android {
    namespace = "info.nightscout.pump.combov2"
    buildFeatures {
        dataBinding = true
    }
}

dependencies {
    api("androidx.databinding:databinding-ktx:${libs.versions.gradlePlugin.get()}")
    implementation(project(":core:data"))
    implementation(project(":core:interfaces"))
    implementation(project(":core:keys"))
    implementation(project(":core:libraries"))
    implementation(project(":core:objects"))
    implementation(project(":core:ui"))
    implementation(project(":core:utils"))
    implementation(project(":core:validators"))
    implementation(project(":pump:combov2:comboctl"))

    api(libs.androidx.lifecycle.viewmodel)
    api(libs.kotlinx.datetime)

    ksp(libs.com.google.dagger.compiler)
    ksp(libs.com.google.dagger.android.processor)
}