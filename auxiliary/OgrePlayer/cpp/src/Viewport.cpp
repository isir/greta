#include "Viewport.h"
#include <OgreViewport.h>

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Viewport__1setBackgroundColour(JNIEnv *, jobject, jlong thisPointer, jlong cp)
{
    Ogre::Viewport* cpp_this = reinterpret_cast<Ogre::Viewport*>(thisPointer);
    Ogre::ColourValue* c = reinterpret_cast<Ogre::ColourValue*>(cp);
    cpp_this->setBackgroundColour(*c);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Viewport_delete(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Viewport* cpp_this = reinterpret_cast<Ogre::Viewport*>(thisPointer);
    delete cpp_this;
}
