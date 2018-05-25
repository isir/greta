#include "Technique.h"
#include <OgreTechnique.h>

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_Technique__1getPass(JNIEnv *, jobject, jlong thisPointer, jint i)
{
    Ogre::Technique* cpp_this = reinterpret_cast<Ogre::Technique*>(thisPointer);
    Ogre::Pass* r = cpp_this->getPass(i);
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Technique_delete(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Technique* cpp_this = reinterpret_cast<Ogre::Technique*>(thisPointer);
    delete cpp_this;
}
