#include "ColourValue.h"
#include <OgreColourValue.h>

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_ColourValue__1instanciate(JNIEnv *, jclass, jdouble r, jdouble g, jdouble b, jdouble a)
{
    Ogre::ColourValue * c = new Ogre::ColourValue(r, g, b, a);
    return reinterpret_cast<jlong>(c);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_ColourValue_delete(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::ColourValue* cpp_this = reinterpret_cast<Ogre::ColourValue*>(thisPointer);
    delete cpp_this;
}
