[![Build Status](https://travis-ci.org/dougszumski/CyclismoProject.svg?branch=gradle_build)](https://travis-ci.org/dougszumski/CyclismoProject)

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

*The error is bad class file magic (cafebabe) or version (0034.0000)*

If compiling from android studio, set the `project byte code version` to `1.7` in:

File -> Other Settings -> Default Settings -> Compiler:Java Compiler
