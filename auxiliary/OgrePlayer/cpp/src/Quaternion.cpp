#include "Quaternion.h"
#include <OgreQuaternion.h>

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_Quaternion__1getIDENTITY(JNIEnv *, jclass)
{
    const Ogre::Quaternion * r = &Ogre::Quaternion::IDENTITY;
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_Quaternion__1instanciate__DDDD(JNIEnv *, jclass, jdouble w, jdouble x, jdouble y, jdouble z)
{
    Ogre::Quaternion * r = new Ogre::Quaternion(w, x, y, z);
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_Quaternion__1instanciate__DJ(JNIEnv *, jclass, jdouble d, jlong op)
{
    Ogre::Vector3 * o = reinterpret_cast<Ogre::Vector3 *>(op);
    Ogre::Quaternion * r = new Ogre::Quaternion(Ogre::Radian(d), *o);
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_Quaternion__1Inverse(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Quaternion* cpp_this = reinterpret_cast<Ogre::Quaternion*>(thisPointer);
    Ogre::Quaternion * r = new Ogre::Quaternion(cpp_this->Inverse());
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT jdouble JNICALL Java_vib_auxiliary_player_ogre_natives_Quaternion__1getw(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Quaternion* cpp_this = reinterpret_cast<Ogre::Quaternion*>(thisPointer);
    return cpp_this->w;
}

JNIEXPORT jdouble JNICALL Java_vib_auxiliary_player_ogre_natives_Quaternion__1getx(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Quaternion* cpp_this = reinterpret_cast<Ogre::Quaternion*>(thisPointer);
    return cpp_this->x;
}

JNIEXPORT jdouble JNICALL Java_vib_auxiliary_player_ogre_natives_Quaternion__1gety(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Quaternion* cpp_this = reinterpret_cast<Ogre::Quaternion*>(thisPointer);
    return cpp_this->y;
}

JNIEXPORT jdouble JNICALL Java_vib_auxiliary_player_ogre_natives_Quaternion__1getz(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Quaternion* cpp_this = reinterpret_cast<Ogre::Quaternion*>(thisPointer);
    return cpp_this->z;
}

JNIEXPORT jdouble JNICALL Java_vib_auxiliary_player_ogre_natives_Quaternion__1getPitch(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Quaternion* cpp_this = reinterpret_cast<Ogre::Quaternion*>(thisPointer);
    return cpp_this->getPitch().valueRadians();
}

JNIEXPORT jdouble JNICALL Java_vib_auxiliary_player_ogre_natives_Quaternion__1getYaw(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Quaternion* cpp_this = reinterpret_cast<Ogre::Quaternion*>(thisPointer);
    return cpp_this->getYaw().valueRadians();
}

JNIEXPORT jdouble JNICALL Java_vib_auxiliary_player_ogre_natives_Quaternion__1getRoll(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Quaternion* cpp_this = reinterpret_cast<Ogre::Quaternion*>(thisPointer);
    return cpp_this->getRoll().valueRadians();
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Quaternion_delete(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Quaternion* cpp_this = reinterpret_cast<Ogre::Quaternion*>(thisPointer);
    delete cpp_this;
}
