// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

/*    // ✨ 아래와 같이 alias를 사용하도록 수정합니다.
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.kotlin.kapt) apply false*/
    id("com.google.dagger.hilt.android") version "2.48" apply false //hilt찾을 수 없다는 오류가 나서 버전을 추가함.
    //버전 추가하니까 된다!!!!!!

}