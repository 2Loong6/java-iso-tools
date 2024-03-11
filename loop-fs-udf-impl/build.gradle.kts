plugins {
    `java-library`
}

dependencies {
    api(projects.loopFsSpi)
}

tasks.jar {
    manifest {
        attributes("Automatic-Module-Name" to "com.github.stephenc.javaisotools.loopfs.udf")
    }
}
