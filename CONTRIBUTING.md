Building with Maven
=========

Prerequisites
----

###Clone the `maven-android-sdk-deployer`
```sh
git clone git@github.com:mosabua/maven-android-sdk-deployer.git
cd maven-android-sdk-deployer
```

###Install the `android artifact`

```sh
mvn install -P 4.4 -Dplatform.android.groupid=com.google.android -Dplatform.android.artifactid=android
```

###Install the `android support artifact`

```sh
cd compatibility-v4
mvn install -Dextras.compatibility.v4.groupid=com.google.android -Dextras.compatibility.v4.artifactid=support-v4
```

###Install the `android appcompat artifact`

```sh
cd ../compatibility-v7-appcompat
mvn install
```

###Update `settings.xml`

This is only needed for the release build.
In your *maven home* directory, update the `settings.xml` file with signing information:
```xml
 <settings>
    <profiles>
        <profile>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <properties>
                <sign.keystore>PATH_TO_YOUR_RELEASE_KEYSTORE</sign.keystore>
                <sign.alias>YOUR_ALIAS</sign.alias>
                <sign.keypass>YOUR_KEY_PASSOWRD</sign.keypass>
                <sign.storepass>YOUR_STORE_PASSOWRD</sign.storepass>
            </properties>    
        </profile>
    </profiles>
</settings>
```
Building
----

In the main folder of the project, execute:

```sh
mvn install -DskipTests
```

This will create a *jar*, *sources* and *javadoc* for both projects (**library** and **example**), *apklib* for the **library** and debuggable *apk* for the **example**.

Building release version
----

In the main folder of the project, execute:

```sh
mvn install -DskipTests -P release
```

This will create a *jar*, *sources* and *javadoc* for both projects (**library** and **example**), *apklib* for the **library** and a non-debuggable, zipaligned * apk* for the **example**. All artifacts will be signed with your signature.