pluginManagement {
    repositories {
        google() // Blok 'content' yang membatasi sudah dihapus
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") } // Menggunakan sintaks 'uri' Anda
    }
}

rootProject.name = "WANGKU"
include(":app")