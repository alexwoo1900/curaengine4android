README: [ENGLISH](https://github.com/alexwoo1900/curaengine4android/blob/main/README.md) | [简体中文](https://github.com/alexwoo1900/curaengine4android/blob/main/README_CN.md)

# CuraEngine for Android

[![License](https://img.shields.io/github/license/alexwoo1900/curaengine4android)](https://www.gnu.org/licenses/agpl-3.0)

如果你还不知道什么是CuraEngine，请先阅读该 [项目文档](https://github.com/Ultimaker/CuraEngine)。  

本项目旨在演示如何在Android平台上运行CuraEngine。

## Demo

![CuraEngine for Android](https://github.com/alexwoo1900/curaengine4android/blob/main/docs/assets/curaengine4android.gif)

## 快速上手

- 下载本项目[最新版本](https://github.com/alexwoo1900/curaengine4android/archive/refs/heads/main.zip)并把包解压到`ce4a`
- 在Android Studio中创建C++项目
    - New Project -> Native C++
        - Language: Kotlin
        - Minimum SDK: Android 6.0
    - 初始化Android环境 (自动完成)
    - 创建AVD
        - 复制`ce4a\com.example.myapplication`到AVD的`/mnt/sdcard/android/data`下
- 用`ce4a\src`替换`~\AndroidStudioProjects\{PROJECT}\app\src`
- 用`ce4a\build.gradle`替换`~\AndroidStudioProjects\{PROJECT}\app\build.gradle`
- 运行项目

PS.
假如你在构建的过程中遇到问题，请删除`~\AndroidStudioProjects\{PROJECT}\app\.cxx`后重试。

### 定制化

为了简化项目，我将大部分CuraEngine所需的参数硬编码进了代码里。  
你可以重新编写更加灵活的引擎运行函数，也可以直接将`Java_com_example_myapplication_MainActivity_runCuraEngine`中的变量替换成你自己的字符串。

## 项目内容

### CuraEngine源码

CuraEngine原本就是为桌面平台设计的，我们没法在Android上直接使用它的源码。  
尽管Android Studio支持C++让开发者不再需要为移植编写底层代码，但对于CuraEngine的原生缺陷，比如[#1142](https://github.com/Ultimaker/CuraEngine/issues/1142)，还是需要我们人工修复。  

本项目中的CuraEngine源码是我个人的修订版本，里面已经修复了包含[#1142](https://github.com/Ultimaker/CuraEngine/issues/1142)在内的几个恶性bug。

### 构建脚本

如果你只想原样地使用CuraEngine，那不用修改任何东西。  
为了让开发者尽快地把项目跑起来，我已在项目中移除了所有非必要的依赖库。  
假如你还是需要Arcus或者是Open MPI等依赖项目，只需要把它们的代码放进项目中，然后修改CMakeLists.txt即可。

### Kotlin应用

我做了简单的应用页面来展示CuraEngine的可用性。它由Android Studio原生支持的Kotlin和C++混编。
其中C++部分的代码可以让你很清楚地了解到如何从CuraEngine中提取运行信息以及和前端交互。