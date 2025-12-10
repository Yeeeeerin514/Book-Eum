plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    // ✨ 핵심 1: Kapt 플러그인 적용 (없으면 kapt 종속성 정의 오류)
    //id("kotlin-kapt")
    // ✨ 핵심 2: Hilt 플러그인 적용 (Hilt DI 코드를 생성하기 위해 필요)

    // 1. Kapt 플러그인 (필수)
    kotlin("kapt")

    // 2. Hilt 플러그인 (필수)
    id("com.google.dagger.hilt.android")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
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
    //implementation(libs.androidx.navigation.fragment.ktx)
    //implementation(libs.androidx.navigation.ui.ktx)
    // navigation-compose는 최신 버전 하나만 남깁니다.
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.window)
    //implementation(libs.androidx.room.compiler)

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
    //implementation("com.folioreader:folioreader:0.6.0")

    // 또는 Android EPUB3 라이브러리 사용
    //implementation("org.readium:readium-shared:2.2.0")
    //implementation("org.readium:readium-streamer:2.2.0")

    // PDF 및 다양한 포맷 지원을 위한 대안
    //implementation("com.github.barteksc:android-pdf-viewer:3.2.0-beta.1")

    // Hilt: 의존성 주입 라이브러리 (자동으로 객체 생성/관리)
    implementation("com.google.dagger:hilt-android:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")  // ✅ 추가!
    //kotlin("kapt")  //hilt플러그인은 얘 뒤에 하는게 좋다고 함. 얘 빌드 최상위 파일이 아니라 여기에 하니까 됨!!!!!!!
    kapt("com.google.dagger:hilt-compiler:2.48")
    kapt(libs.androidx.room.compiler)
    //kapt("androidx.hilt:hilt-compiler:1.1.0")


    // ===== ExoPlayer for 음악 재생 =====
    val media3Version = "1.2.0"
    implementation("androidx.media3:media3-exoplayer:$media3Version")
    implementation("androidx.media3:media3-ui:$media3Version")
    implementation("androidx.media3:media3-common:$media3Version")
    implementation("androidx.media3:media3-session:$media3Version")

    // ===== Lifecycle & ViewModel =====
    val lifecycleVersion = "2.6.2"
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:$lifecycleVersion")

    // ===== Coroutines =====
    val coroutinesVersion = "1.7.3"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

    // ===== Coil for 이미지 로딩 (앨범 아트용) =====
    implementation("io.coil-kt:coil-compose:2.5.0")


    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    implementation("com.google.accompanist:accompanist-webview:0.34.0")

    // SLF4J API (Logger, LoggerFactory 등)
    implementation("org.slf4j:slf4j-api:1.7.32")

    // SLF4J의 실제 로깅 구현체 (이것이 없으면 경고 메시지가 뜸)
    implementation("org.slf4j:slf4j-simple:1.7.32")


}

kapt {
    correctErrorTypes = true
}
