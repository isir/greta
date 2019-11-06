#include "VertexAnimationTrack.h"
#include <OgreAnimationTrack.h>

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_VertexAnimationTrack__1createVertexPoseKeyFrame(JNIEnv *, jobject, jlong thisPointer, jfloat t)
{
    Ogre::VertexAnimationTrack* cpp_this = reinterpret_cast<Ogre::VertexAnimationTrack*>(thisPointer);
    Ogre::VertexPoseKeyFrame * r = cpp_this->createVertexPoseKeyFrame(t);
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_VertexAnimationTrack_delete(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::VertexAnimationTrack* cpp_this = reinterpret_cast<Ogre::VertexAnimationTrack*>(thisPointer);
    delete cpp_this;
}
