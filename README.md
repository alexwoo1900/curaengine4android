README: [ENGLISH](https://github.com/alexwoo1900/curaengine4android/blob/main/README.md) | [简体中文](https://github.com/alexwoo1900/curaengine4android/blob/main/README_CN.md)

## CuraEngine for Android

If you know nothing about CuraEngine, please read it's [document](https://github.com/Ultimaker/CuraEngine) first.  

This project can tell you how to run CuraEngine on Android.

## Demo

![CuraEngine for Android](https://github.com/alexwoo1900/curaengine4android/blob/main/docs/assets/curaengine4android.gif)

## Quick Start

- Download the [latest release](https://github.com/alexwoo1900/curaengine4android/archive/refs/heads/main.zip) and extract the package into `[CURAENGINE4ANDROID]`
- Create a new C++ project in Android Studio
    - New Project -> Native C++
        - Language: Kotlin
        - Minimum SDK: Android 6.0
    - Initialize Android environment (automatic)
    - Setup your AVD
        - Copy `[CURAENGINE4ANDROID]\com.example.myapplication` into `/mnt/sdcard/android/data` in AVD
- Replace `[HOME]\AndroidStudioProjects\[PROJECT]\app\src` with `[CURAENGINE4ANDROID]\src`
- Replace `[HOME]\AndroidStudioProjects\[PROJECT]\app\build.gradle` with `[CURAENGINE4ANDROID]\build.gradle`
- Build and run

PS.
If you met any problems during building stage, delete `[HOME]\AndroidStudioProjects\[PROJECT]\app\.cxx` and retry.

### Customization

To simplify the demo, I hardcored all engine variables into the function.  
You can rewrite more flexible engine running functions, or just replace the parameters in `Java_com_example_myapplication_MainActivity_runCuraEngine` with your own words.

## What's included

### CuraEngine source code

We cannot use the source code of CuraEngine directly in Android Studio because CuraEngine was originally designed for the PC platform.  
Although we no longer have to write the underlying migration code for CuraEngine (Android Studio support C++ code), but we still need to fix some problems caused by architectural differences, like [#1142](https://github.com/Ultimaker/CuraEngine/issues/1142).   

The source code of CuraEngine in this project is a revised version, which has fixed several bugs including [#1142](https://github.com/Ultimaker/CuraEngine/issues/1142).

### Build Script

If you just use the engine as its, you don't have to modify any files.  
I have removed all unnecessary dependency libraries to allow developers to run the demo as soon as possible.  
If you need Arcus or Open MPI, just download the code, put them into your project and add some configurations to your CmakeLists.txt

### Kotlin Demo

I make a very simple application page to visually show developers the availability of CuraEngine.  
It's written in Kotlin and C++, and both are natively supported by Android Studio.  
Especially the C++ part of the code can let you know how to extract information from CuraEngine and interacts with frontend.  