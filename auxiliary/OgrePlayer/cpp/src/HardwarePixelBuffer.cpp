#include "HardwarePixelBuffer.h"
#include <OgreHardwarePixelBuffer.h>

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_HardwarePixelBuffer__1getRenderTarget(JNIEnv *, jobject, jlong thisPointer, jint i)
{
    Ogre::HardwarePixelBuffer* cpp_this = reinterpret_cast<Ogre::HardwarePixelBuffer*>(thisPointer);
    Ogre::RenderTexture* r = cpp_this->getRenderTarget(i);
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_HardwarePixelBuffer__1blitToMemory(JNIEnv *, jobject, jlong thisPointer, jlong op)
{
    Ogre::HardwarePixelBuffer* cpp_this = reinterpret_cast<Ogre::HardwarePixelBuffer*>(thisPointer);
    Ogre::PixelBox* o = reinterpret_cast<Ogre::PixelBox*>(op);
    cpp_this->blitToMemory(*o);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_HardwarePixelBuffer_delete(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::HardwarePixelBuffer* cpp_this = reinterpret_cast<Ogre::HardwarePixelBuffer*>(thisPointer);
    delete cpp_this;
}
