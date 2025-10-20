plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    // общие зависимости для всех JVM-модулей можно добавить здесь при желании
    // implementation(libs.coroutines.core)
}
