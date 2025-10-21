pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        // можно оставить, даже если Compose сейчас не используем
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}
rootProject.name = "wallet-core-starter"
include(":app", ":utils")
