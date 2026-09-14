plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.cryptogay"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.cryptogay"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
    }

    testOptions {
        unitTests.all {
            it.jvmArgs("-Dnet.bytebuddy.experimental=true")
        }
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)

    // Retrofit & OkHttp & Gson
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)

    // Lifecycle ViewModel & LiveData
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)

    // Navigation Component
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // SwipeRefreshLayout
    implementation(libs.swiperefreshlayout)

    // Image loading
    implementation(libs.glide)

    // MPAndroidChart
    implementation(libs.mpandroidchart)

    // Room Database
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.core.testing)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}