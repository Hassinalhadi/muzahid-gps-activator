plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "dev.muzahid.activator"
    compileSdk = 34

    defaultConfig {
        applicationId = "dev.muzahid.activator"
        minSdk = 26
        targetSdk = 34
        versionCode = 100
        versionName = "1.0.0-VIP"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    sourceSets {
        getByName("main") {
            resources.srcDirs("src/main/resources")
        }
    }
    packaging {
        resources {
            merges += "META-INF/xposed/**"
            pickFirsts += "META-INF/xposed/**"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.cardview:cardview:1.0.0")

    compileOnly("io.github.libxposed:api:102.0.0")
}
