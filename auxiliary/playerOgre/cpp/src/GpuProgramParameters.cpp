#include "GpuProgramParameters.h"
#include <OgreGpuProgramParams.h>
#include "_Object_.h"

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_GpuProgramParameters__1setNamedConstant_1int_1star(JNIEnv *env, jobject, jlong thisPointer, jstring id, jlong p, jint count, jint mul)
{
    Ogre::GpuProgramParameters* cpp_this = reinterpret_cast<Ogre::GpuProgramParameters*>(thisPointer);
    Ogre::String oid = ""; convertJStringToOgreString(env, id, oid);
    int* buff = reinterpret_cast<int*>(p);
    cpp_this->setNamedConstant(oid, buff, count, mul);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_GpuProgramParameters__1setNamedConstant_1float_1star(JNIEnv *env, jobject, jlong thisPointer, jstring id, jlong p, jint count, jint mul)
{
    Ogre::GpuProgramParameters* cpp_this = reinterpret_cast<Ogre::GpuProgramParameters*>(thisPointer);
    Ogre::String oid = ""; convertJStringToOgreString(env, id, oid);
    float* buff = reinterpret_cast<float*>(p);
    cpp_this->setNamedConstant(oid, buff, count, mul);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_GpuProgramParameters__1setNamedConstant(JNIEnv *env, jobject, jlong thisPointer, jstring id, jint i)
{
    Ogre::GpuProgramParameters* cpp_this = reinterpret_cast<Ogre::GpuProgramParameters*>(thisPointer);
    Ogre::String oid = ""; convertJStringToOgreString(env, id, oid);
    cpp_this->setNamedConstant(oid, i);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_GpuProgramParameters_delete(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::GpuProgramParameters* cpp_this = reinterpret_cast<Ogre::GpuProgramParameters*>(thisPointer);
    delete cpp_this;
}
