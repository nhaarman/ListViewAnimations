ListViewAnimations Release Process
=================================

 1. Make sure it builds!

        gradlew clean build

 2. Check the version number in the root `gradle.properties`.
 3. Make the release!

        gradlew clean generateReleaseJavadoc pushMaven -DisRelease=true

 4. Promote the Maven artifact on Sonatype's OSS Nexus install.
 5. Tag commit as release (`x.x.x`)
 6. Provide changelog / `.jar` files at [the Releases page](https://github.com/nhaarman/ListViewAnimations/releases).
 7. Increment the patch version number for future SNAPSHOT releases in the root `gradle.properties`.