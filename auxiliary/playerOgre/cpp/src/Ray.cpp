#include "Ray.h"
#include <OgreRay.h>

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_Ray__1instanciate(JNIEnv *, jclass)
{
    Ogre::Ray * r = new Ogre::Ray();
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_Ray__1getPoint(JNIEnv *, jobject, jlong thisPointer, jdouble d)
{
    Ogre::Ray* cpp_this = reinterpret_cast<Ogre::Ray*>(thisPointer);
    Ogre::Vector3 * r = new Ogre::Vector3(cpp_this->getPoint(d));
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Ray_delete(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Ray* cpp_this = reinterpret_cast<Ogre::Ray*>(thisPointer);
    delete cpp_this;
}
