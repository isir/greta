#include "AnimationState.h"
#include <OgreAnimationState.h>

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_AnimationState__1setTimePosition(JNIEnv *, jobject, jlong thisPointer, jdouble d)
{
    Ogre::AnimationState* cpp_this = reinterpret_cast<Ogre::AnimationState*>(thisPointer);
    cpp_this->setTimePosition((Ogre::Real)d);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_AnimationState__1setEnabled(JNIEnv *, jobject, jlong thisPointer, jboolean b)
{
    Ogre::AnimationState* cpp_this = reinterpret_cast<Ogre::AnimationState*>(thisPointer);
    cpp_this->setEnabled(b);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_AnimationState__1getParent_1notifyDirty(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::AnimationState* cpp_this = reinterpret_cast<Ogre::AnimationState*>(thisPointer);
    cpp_this->getParent()->_notifyDirty();
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_AnimationState_delete(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::AnimationState* cpp_this = reinterpret_cast<Ogre::AnimationState*>(thisPointer);
    delete cpp_this;
}
