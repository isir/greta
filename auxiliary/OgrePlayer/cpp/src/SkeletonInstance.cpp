#include "SkeletonInstance.h"
#include <OgreSkeletonInstance.h>
#include "_Object_.h"

JNIEXPORT jboolean JNICALL Java_vib_auxiliary_player_ogre_natives_SkeletonInstance__1hasBone(JNIEnv *env, jobject, jlong thisPointer, jstring s)
{
    Ogre::SkeletonInstance* cpp_this = reinterpret_cast<Ogre::SkeletonInstance*>(thisPointer);
    Ogre::String os = ""; convertJStringToOgreString(env, s, os);
    return cpp_this->hasBone(os);
}

JNIEXPORT jint JNICALL Java_vib_auxiliary_player_ogre_natives_SkeletonInstance__1getNumBones(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::SkeletonInstance* cpp_this = reinterpret_cast<Ogre::SkeletonInstance*>(thisPointer);
    return cpp_this->getNumBones();
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_SkeletonInstance__1getBone__JLjava_lang_String_2(JNIEnv *env, jobject, jlong thisPointer, jstring s)
{
    Ogre::SkeletonInstance* cpp_this = reinterpret_cast<Ogre::SkeletonInstance*>(thisPointer);
    Ogre::String os = ""; convertJStringToOgreString(env, s, os);
    Ogre::Bone* r = cpp_this->getBone(os);
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_SkeletonInstance__1getBone__JI(JNIEnv *, jobject, jlong thisPointer, jint i)
{
    Ogre::SkeletonInstance* cpp_this = reinterpret_cast<Ogre::SkeletonInstance*>(thisPointer);
    Ogre::Bone* r = cpp_this->getBone(i);
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_SkeletonInstance__1getRootBone(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::SkeletonInstance* cpp_this = reinterpret_cast<Ogre::SkeletonInstance*>(thisPointer);
    Ogre::Bone* r = cpp_this->getRootBone();
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_SkeletonInstance_delete(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::SkeletonInstance* cpp_this = reinterpret_cast<Ogre::SkeletonInstance*>(thisPointer);
    delete cpp_this;
}
