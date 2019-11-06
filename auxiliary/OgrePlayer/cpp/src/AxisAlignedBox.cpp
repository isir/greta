#include "AxisAlignedBox.h"
#include <OgreAxisAlignedBox.h>

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_AxisAlignedBox__1getCenter(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::AxisAlignedBox* cpp_this = reinterpret_cast<Ogre::AxisAlignedBox*>(thisPointer);
    Ogre::Vector3 * v = new Ogre::Vector3(cpp_this->getCenter());
    return reinterpret_cast<jlong>(v);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_AxisAlignedBox__1getMinimum(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::AxisAlignedBox* cpp_this = reinterpret_cast<Ogre::AxisAlignedBox*>(thisPointer);
    Ogre::Vector3 * v = &cpp_this->getMinimum();
    return reinterpret_cast<jlong>(v);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_AxisAlignedBox__1getMaximum(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::AxisAlignedBox* cpp_this = reinterpret_cast<Ogre::AxisAlignedBox*>(thisPointer);
    Ogre::Vector3 * v = &cpp_this->getMaximum();
    return reinterpret_cast<jlong>(v);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_AxisAlignedBox__1setInfinite(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::AxisAlignedBox* cpp_this = reinterpret_cast<Ogre::AxisAlignedBox*>(thisPointer);
    cpp_this->setInfinite();
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_AxisAlignedBox_delete(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::AxisAlignedBox* cpp_this = reinterpret_cast<Ogre::AxisAlignedBox*>(thisPointer);
    delete cpp_this;
}
