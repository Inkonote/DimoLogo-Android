Dimo Logo
===
[![GitHub tag](https://shields.dev.inkonote.com/github/tag/ty0x2333/DimoLogo-Android.svg)]()

Usage
---
Add it in your root build.gradle at the end of repositories:

```
allprojects {
    repositories {
        ...
        maven {
            url "http://artifactory.dev.inkonote.com/artifactory/libs-release-local"
            credentials {
                username = "${artifactory_user}"
                password = "${artifactory_password}"
            }
        }
    }
}
```

Add the dependency

```
dependencies {
    implementation 'sh.tyy:dimologo:1.0.0'
}
```