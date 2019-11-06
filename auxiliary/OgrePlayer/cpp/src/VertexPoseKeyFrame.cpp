#include "VertexPoseKeyFrame.h"
#include <OgreKeyFrame.h>

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_VertexPoseKeyFrame__1addPoseReference(JNIEnv *, jobject, jlong thisPointer, jint i, jfloat f)
{
    Ogre::VertexPoseKeyFrame* cpp_this = reinterpret_cast<Ogre::VertexPoseKeyFrame*>(thisPointer);
    cpp_this->addPoseReference(i, f);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_VertexPoseKeyFrame__1updatePoseReference(JNIEnv *, jobject, jlong thisPointer, jint i, jfloat f)
{
    Ogre::VertexPoseKeyFrame* cpp_this = reinterpret_cast<Ogre::VertexPoseKeyFrame*>(thisPointer);
    cpp_this->updatePoseReference(i, f);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_VertexPoseKeyFrame_delete(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::VertexPoseKeyFrame* cpp_this = reinterpret_cast<Ogre::VertexPoseKeyFrame*>(thisPointer);
    delete cpp_this;
}
