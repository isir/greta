#include "Pose.h"
#include <OgrePose.h>
#include "_Object_.h"

JNIEXPORT jstring JNICALL Java_vib_auxiliary_player_ogre_natives_Pose__1getName(JNIEnv *env, jobject, jlong thisPointer)
{
    Ogre::Pose* cpp_this = reinterpret_cast<Ogre::Pose*>(thisPointer);
    return convertOgreStringToJString(env, cpp_this->getName());
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Pose_delete(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Pose* cpp_this = reinterpret_cast<Ogre::Pose*>(thisPointer);
    delete cpp_this;
}
