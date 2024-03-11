plugins {
    `java-library`
}

dependencies {
    api(projects.loopFsIsoImpl)

    api(libs.commons.logging)
    api(libs.commons.vfs)
}
