#include "Animation.h"
#include <OgreAnimation.h>

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_Animation__1createVertexTrack_1VAT_1POSE(JNIEnv *, jobject, jlong thisPointer, jint handle)
{
    Ogre::Animation* cpp_this = reinterpret_cast<Ogre::Animation*>(thisPointer);
    Ogre::VertexAnimationTrack* cpp_return = cpp_this->createVertexTrack((unsigned short)handle, Ogre::VertexAnimationType::VAT_POSE);
    return reinterpret_cast<jlong>(cpp_return);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_Animation__1getVertexTrack(JNIEnv *, jobject, jlong thisPointer, jint handle)
{
    Ogre::Animation* cpp_this = reinterpret_cast<Ogre::Animation*>(thisPointer);
    Ogre::VertexAnimationTrack* cpp_return = cpp_this->getVertexTrack((unsigned short)handle);
    return reinterpret_cast<jlong>(cpp_return);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Animation_delete(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Animation* cpp_this = reinterpret_cast<Ogre::Animation*>(thisPointer);
    delete cpp_this;
}
