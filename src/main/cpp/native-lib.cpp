#include <jni.h>
#include <unistd.h>
#include <iostream>
#include <csignal>
#include <android/log.h>

#if defined(__linux__) || (defined(__APPLE__) && defined(__MACH__))
#include <sys/resource.h>
#endif

#include "src/Application.h"
#include "src/utils/logoutput.h"

enum SliceStage {
    PREPARING = 1,
    INSETS = 0b0010,
    SKINSINFILL = 0b0100,
    GCODEWRITER = 0b1000,
    FINISHED = 0b1111
};

extern "C" JNIEXPORT jint JNICALL
Java_com_example_myapplication_MainActivity_runCuraEngine(
        JNIEnv* env,
        jobject,
        jstring name) {
    char modelSTLPath[256];
    char modelGcodePath[256];

    std::string modelName = env->GetStringUTFChars(name, 0);
    sprintf(modelSTLPath, "/mnt/sdcard/Android/data/com.example.myapplication/model/%s.stl", modelName.c_str());
    sprintf(modelGcodePath, "/mnt/sdcard/Android/data/com.example.myapplication/output/%s.gcode", modelName.c_str());

    const char* myargv[13] = {"CuraEngine", "slice", "-v", "-j","/mnt/sdcard/Android/data/com.example.myapplication/definitions/fdmprinter.def.json", "-v", "-j", "/mnt/sdcard/Android/data/com.example.myapplication/definitions/fdmextruder.def.json","-o", modelGcodePath,"-e1","-l", modelSTLPath};
    int myargc = 13;
    using namespace cura;
    cura::Application::getInstance().run((unsigned int)myargc, (char**)myargv);
    return 0;
}

static int current_stage = PREPARING;
static int current_progress = 0;

extern "C" JNIEXPORT jint JNICALL
Java_com_example_myapplication_MainActivity_monitorCuraEngine(
        JNIEnv* env,
        jobject) {
    int pipes[2];
    pipe(pipes);
    dup2(pipes[1], STDERR_FILENO);
    close(pipes[1]);

    int p1 = 0, p2 = 0, p3 = 0, t1 = 0, t2 = 0, t3 = 0, s = 0;
    FILE *inputFile = fdopen(pipes[0], "r");
    char readBuffer[256];
    while (true) {
        fgets(readBuffer, sizeof(readBuffer), inputFile);
        if (sscanf(readBuffer, "[DEBUG] Processing insets for layer %d of %d\n", &p1, &t1) == 2) {
            current_stage  = INSETS;
            current_progress  = p1 * 100 / t1;
        } else if (sscanf(readBuffer, "[DEBUG] Processing skins and infill layer %d of %d\n", &p2, &t2) == 2) {
            current_stage  = SKINSINFILL;
            current_progress  = p2 * 100 / t2;
        } else if (sscanf(readBuffer, "[DEBUG] GcodeWriter processing layer %d of %d\n", &p3, &t3) == 2) {
            current_stage  = GCODEWRITER;
            current_progress  = p3 * 100 / t3;
        } else if (sscanf(readBuffer, "Print time (s): %d\n", &s) == 1) {
            current_stage  = FINISHED;
            break;
        } else {
            __android_log_write(2, "stderr", readBuffer);
        }
    }
    return 0;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_example_myapplication_MainActivity_getSliceStageFromCuraEngine(
        JNIEnv* env,
        jobject) {
    return current_stage;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_example_myapplication_MainActivity_getSliceProgressFromCuraEngine(
        JNIEnv* env,
        jobject) {
    return current_progress;
}