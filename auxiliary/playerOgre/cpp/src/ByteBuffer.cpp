#include "ByteBuffer.h"

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_ByteBuffer__1instanciate(JNIEnv *, jclass, jint size)
{
    char* tab = new char[size];
    return reinterpret_cast<jlong>(tab);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_ByteBuffer__1updateJavaBuffer(JNIEnv *env, jclass, jlong thisPointer, jbyteArray buff, jint size)
{
    char* cpp_this = reinterpret_cast<char*>(thisPointer);
    env->SetByteArrayRegion(buff, 0, size, (jbyte *)cpp_this);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_ByteBuffer__1setIndex(JNIEnv *, jobject, jlong  thisPointer, jint index, jbyte val)
{
    char* cpp_this = reinterpret_cast<char*>(thisPointer);
    cpp_this[index] = val;
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_ByteBuffer_delete(JNIEnv *, jobject, jlong thisPointer)
{
    char* cpp_this = reinterpret_cast<char*>(thisPointer);
    delete[] cpp_this;
}
