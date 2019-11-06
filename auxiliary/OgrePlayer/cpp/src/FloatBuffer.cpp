#include "FloatBuffer.h"

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_FloatBuffer__1instanciate(JNIEnv *, jclass, jint size)
{
    float* tab = new float[size];
    return reinterpret_cast<jlong>(tab);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_FloatBuffer__1updateJavaBuffer(JNIEnv *env, jclass, jlong thisPointer, jfloatArray buff, jint size)
{
    float* cpp_this = reinterpret_cast<float*>(thisPointer);
    env->SetFloatArrayRegion(buff, 0, size, (jfloat *)cpp_this);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_FloatBuffer__1setIndex(JNIEnv *, jobject, jlong thisPointer, jint index, jfloat val)
{
    float* cpp_this = reinterpret_cast<float*>(thisPointer);
    cpp_this[index] = val;
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_FloatBuffer_delete(JNIEnv *, jobject, jlong thisPointer)
{
    float* cpp_this = reinterpret_cast<float*>(thisPointer);
    delete[] cpp_this;
}
