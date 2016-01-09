[![Build Status](https://travis-ci.org/fluxoid-org/CyclismoProject.svg?branch=devel)](https://travis-ci.org/fluxoid-org/CyclismoProject)

Build instructions
------------------

Set up the android sdk. In particular, make sure you have set the ANDROID_HOME environmental variable, eg:

```
export ANDROID_HOME=$HOME/android-sdks
```

run: 

```bash

./gradlew build

```

or import to android studio. 

Troubleshooting
------------------

###The error is bad class file magic (cafebabe) or version (0034.0000)###

If compiling from android studio, set the `project byte code version` to `1.7` in:

`File -> Other Settings -> Default Settings -> Compiler:Java Compiler`

###Android studio has missing dependency on JTurbo after initial import###

temporarily delete `Cyclismo/libs/JTurbo/settings.gradle`, reimport the the project,
and then restore `Cyclismo/libs/JTurbo/settings.gradle`

See: [this bug](https://code.google.com/p/android/issues/detail?id=52962) for more info
