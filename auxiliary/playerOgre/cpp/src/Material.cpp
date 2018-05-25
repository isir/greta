#include "Material.h"
#include <OgreMaterial.h>
#include "_Object_.h"

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_Material__1getTechnique(JNIEnv *, jobject, jlong thisPointer, jint i)
{
    Ogre::Material* cpp_this = reinterpret_cast<Ogre::Material*>(thisPointer);
    Ogre::Technique * r = cpp_this->getTechnique(i);
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_Material__1clone(JNIEnv *env, jclass, jlong thisPointer, jstring s, jboolean b, jstring s2)
{
    Ogre::Material* cpp_this = reinterpret_cast<Ogre::Material*>(thisPointer);
    Ogre::String os = ""; convertJStringToOgreString(env, s, os);
    Ogre::String os2 = ""; convertJStringToOgreString(env, s2, os2);
    Ogre::Material* clone = cpp_this->clone(os, b, os2).getPointer();
    return reinterpret_cast<jlong>(clone);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Material_delete(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Material* cpp_this = reinterpret_cast<Ogre::Material*>(thisPointer);
    delete cpp_this;
}
