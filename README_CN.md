README: [ENGLISH](https://github.com/alexwoo1900/curaengine4android/blob/main/README.md) | [简体中文](https://github.com/alexwoo1900/curaengine4android/blob/main/README_CN.md)

## CuraEngine for Android

如果你还不知道什么是CuraEngine，请先阅读该 [项目文档](https://github.com/Ultimaker/CuraEngine)。  

本项目旨在演示如何在Android平台上运行CuraEngine。

## Demo

![CuraEngine for Android](https://github.com/alexwoo1900/curaengine4android/blob/main/docs/assets/curaengine4android.gif)

## 快速上手

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

### 定制化

为了简化项目，我将大部分CuraEngine所需的参数硬编码进了代码里。  
你可以重新编写更加灵活的引擎运行函数，也可以直接将`Java_com_example_myapplication_MainActivity_runCuraEngine`中的变量替换成你自己的字符串。

## 项目内容

### CuraEngine源码

CuraEngine原本就是为桌面平台设计的，我们没法在Android上直接使用它的源码。  
尽管Android Studio支持C++让开发者不再需要为移植编写底层代码，但对于CuraEngine的原生缺陷，比如[#1142](https://github.com/Ultimaker/CuraEngine/issues/1142)，还是需要我们人工修复。  

本项目中的CuraEngine源码是一个修订版本，里面已经修复了包含[#1142](https://github.com/Ultimaker/CuraEngine/issues/1142)在内的几个bug。

### 构建脚本

如果你只需要原样地使用CuraEngine，那不用修改任何东西。  
为了让开发者尽快地把项目跑起来，我已在项目中移除了所有非必要的依赖库。  
假如你还是需要Arcus或者是Open MPI等依赖项目，只需要把它们的代码放进项目中，然后修改CMakeLists.txt即可。

### Kotlin应用

我做了简单的应用页面来展示CuraEngine的可用性。它由Kotlin和C++混编，被Android Studio原生支持。
特别是C++部分的代码，它可以让你很清楚地了解到如何从CuraEngine中提取运行信息以及如何同前端交互。