plugins {
    `java-library`
}

dependencies {
    api(projects.loopFsSpi)

    implementation(libs.commons.logging)
    implementation(libs.commons.io)

    implementation(libs.hadoop.hdfs)
    implementation(libs.hadoop.common)
    testImplementation(libs.hadoop.minicluster)

    testImplementation(libs.junit)
    testImplementation(libs.assertj)
}
