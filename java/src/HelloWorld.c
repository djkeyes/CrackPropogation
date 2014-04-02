

// this is based off of http://www.csharp.com/javacfort.html

#include <stdio.h>
#include "HelloWorld.h"

// prototype for fortran code
extern int fortranmax_(int *, int []);

JNIEXPORT jint JNICALL Java_HelloWorld_max(JNIEnv* env, jobject obj, jintArray ja) {

    jsize n = (*env)->GetArrayLength(env, ja);
    jint *a = (*env)->GetIntArrayElements(env, ja, 0);

	int result = fortranmax_(&n, a);

//  Instead of ending as a normal C program, the pointers must be cleared
//  before returning to Java.
    (*env)->ReleaseIntArrayElements(env, ja, a, 0);
    return result;
}
