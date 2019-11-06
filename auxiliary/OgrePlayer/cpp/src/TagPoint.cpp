#include "TagPoint.h"
#include <OgreTagPoint.h>

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_TagPoint__1scale(JNIEnv *, jobject, jlong thisPointer, jdouble x, jdouble y, jdouble z)
{
    Ogre::TagPoint* cpp_this = reinterpret_cast<Ogre::TagPoint*>(thisPointer);
    cpp_this->scale(x, y, z);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_TagPoint__1setOrientation(JNIEnv *, jobject, jlong thisPointer, jlong qp)
{
    Ogre::TagPoint* cpp_this = reinterpret_cast<Ogre::TagPoint*>(thisPointer);
    Ogre::Quaternion* q = reinterpret_cast<Ogre::Quaternion*>(qp);
    cpp_this->setOrientation(*q);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_TagPoint_delete(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::TagPoint* cpp_this = reinterpret_cast<Ogre::TagPoint*>(thisPointer);
    delete cpp_this;
}
