enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven(url = "https://www.jitpack.io")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
        maven(url = "https://www.jitpack.io")
    }
}

rootProject.name = "java-iso-tools"
//include("iso9600-ant-tasks")
//include("iso9660-maven-plugin")
include("iso9660-vfs-impl")
include("iso9660-writer")
include("loop-fs-api")
include("loop-fs-iso-impl")
include("loop-fs-spi")
include("loop-fs-udf-impl")
include("sabre")
include("examples:extract")
