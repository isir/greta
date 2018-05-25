#include "IntBuffer.h"

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_IntBuffer__1instanciate(JNIEnv *, jclass, jint size)
{
    int* tab = new int[size];
    return reinterpret_cast<jlong>(tab);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_IntBuffer__1updateJavaBuffer(JNIEnv *env, jclass, jlong thisPointer, jintArray buff, jint size)
{
    int* cpp_this = reinterpret_cast<int*>(thisPointer);
    jint* temp = new jint[size];
    for (int i = 0; i < size; ++i)
{
        temp[i] = cpp_this[i];
    }
    env->SetIntArrayRegion(buff, 0, size, temp);
    delete[] temp;
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_IntBuffer__1setIndex(JNIEnv *, jobject, jlong thisPointer, jint index, jint val)
{
    int* cpp_this = reinterpret_cast<int*>(thisPointer);
    cpp_this[index] = val;
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_IntBuffer_delete(JNIEnv *, jobject, jlong thisPointer)
{
    int* cpp_this = reinterpret_cast<int*>(thisPointer);
    delete[] cpp_this;
}
