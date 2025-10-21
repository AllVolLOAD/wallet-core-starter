import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.security.MessageDigest


plugins {
    kotlin("jvm")           // версия объявлена в корне
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

// репозитории не указываем — они в settings.gradle.kts

dependencies {
    implementation(project(":utils"))

    // runtime-логгер, чтобы не было NOP в рантайме
    runtimeOnly("org.slf4j:slf4j-simple:2.0.13")
}

application {
    // если файл App.kt в корне пакета: main() в AppKt
    mainClass.set("AppKt")
}

// Опционально: настроим имя файла и манифест
tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveBaseName.set("wallet-cli")
    archiveClassifier.set("")        // без "-all"
    archiveVersion.set("")           // без версии в имени
    // mergeServiceFiles() // раскомментируй, если когда-нибудь добавим gRPC/vertx и т.п.
}

kotlin {
    // Компилируем под JVM 1.8 (чтобы запускалось на Java 8)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
    }
}

java {
    // привести Java-таски к тем же таргетам
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.register("dist") {
    group = "distribution"
    description = "Create distribution package"

    dependsOn("shadowJar")

    doLast {
        val distDir = file("${project.rootDir}/dist")
        val shadowJarTask = tasks.getByName("shadowJar") as ShadowJar
        val jarFile = shadowJarTask.archiveFile.get().asFile

        // Очистка и создание папки dist
        delete(distDir)
        distDir.mkdirs()

        // Копируем JAR
        copy {
            from(jarFile)
            into(distDir)
        }

        // Копируем README
        copy {
            from("${project.rootDir}/README.md")
            into(distDir)
        }

        // Генерируем SHA256 checksum
        val checksumFile = file("${distDir}/wallet-cli.jar.sha256")
        val checksum = MessageDigest.getInstance("SHA-256")
            .digest(jarFile.readBytes())
            .joinToString("") { byte -> "%02x".format(byte) }

        checksumFile.writeText(checksum)

        println("✅ Distribution created in: ${distDir.absolutePath}")
        println("📦 JAR: ${jarFile.name} (${jarFile.length() / 1024} KB)")
        println("🔒 SHA256: $checksum")
    }
}
