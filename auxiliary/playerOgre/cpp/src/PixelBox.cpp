#include "PixelBox.h"
#include <OgrePixelFormat.h>

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_PixelBox__1instanciate(JNIEnv *, jclass, jint w, jint h, jlong buffp)
{
    Ogre::PixelBox* r = new Ogre::PixelBox(w, h, 1, Ogre::PixelFormat::PF_BYTE_RGB, (unsigned char*)buffp);
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_PixelBox_delete(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::PixelBox* cpp_this = reinterpret_cast<Ogre::PixelBox*>(thisPointer);
    delete cpp_this;
}
