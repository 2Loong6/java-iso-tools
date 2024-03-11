plugins {
    `java-library`
}

dependencies {
    api(projects.loopFsApi)

    api(libs.hadoop.client)
}
