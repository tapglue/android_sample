# Tapglue Sample Android App

This will help you get started with Tapglue on iOS by showing a complete app.

A more detailed documentation can be found on our [documentation](http://developers.tapglue.com/docs/android) website.

## Installing with Gradle

To use the SDK in Android we recommend installing with [Gradle](http://www.gradle.org/). This will automatically install the necessary dependencies and pull the SDK binaries from the Maven Central repository.

To install the current stable version add this dependency to your `build.gradle`:

```gradle
repositories {
    jcenter()
    // or mavenCentral()
}

dependencies {
    compile 'com.tapglue.android:tapglue-android-sdk:1.0.0'
}
```

That's it! Build the project try out our app.
