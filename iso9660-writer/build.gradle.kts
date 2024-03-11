plugins {
    `java-library`
}

dependencies {
    api(projects.iso9660VfsImpl)
    api(projects.sabre)

    testImplementation(libs.junit)
    testImplementation(libs.assertj)
}
