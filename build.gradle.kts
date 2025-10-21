plugins {
    // Объявляем версии плагинов ОДИН РАЗ в корне
    kotlin("jvm") version "2.0.20" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.20" apply false
    id("org.jetbrains.compose") version "1.7.0" apply false
}

// Общая настройка toolchain для JVM-модулей (если нужно)
subprojects {

    plugins.withId("org.jetbrains.kotlin.jvm") {
        the<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension>().jvmToolchain(21)
    }

}
