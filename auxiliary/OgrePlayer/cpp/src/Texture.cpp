#include "Texture.h"
#include <OgreTexture.h>
#include <OgreHardwarePixelBuffer.h>

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_Texture__1getBuffer(JNIEnv *, jobject, jlong thisPointer, jint i, jint i2)
{
    Ogre::Texture* cpp_this = reinterpret_cast<Ogre::Texture*>(thisPointer);
    Ogre::HardwarePixelBuffer* r = cpp_this->getBuffer(i, i2).getPointer();
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Texture_delete(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Texture* cpp_this = reinterpret_cast<Ogre::Texture*>(thisPointer);
    delete cpp_this;
}
