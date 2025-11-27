plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.book_m_front"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.book_m_front"
        minSdk = 35
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom)) // Compose BOM이 버전 관리
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation("androidx.compose.material:material-icons-extended")

    //
    implementation(libs.androidx.compose.material3)

    // Navigation 라이브러리들
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    // navigation-compose는 최신 버전 하나만 남깁니다.
    implementation("androidx.navigation:navigation-compose:2.9.5")

    // 테스트 관련 의존성
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // 백엔드 통신을 위해 추가한 종속성 (정리된 버전)
    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // Retrofit Converters
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")



    // FolioReader - EPUB 뷰어 라이브러리
    implementation("com.folioreader:folioreader:0.6.0")

    // 또는 Android EPUB3 라이브러리 사용
    implementation("org.readium:readium-shared:2.2.0")
    implementation("org.readium:readium-streamer:2.2.0")

    // PDF 및 다양한 포맷 지원을 위한 대안
    implementation("com.github.barteksc:android-pdf-viewer:3.2.0-beta.1")

}