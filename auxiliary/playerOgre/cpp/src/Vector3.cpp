#include "Vector3.h"
#include <OgreVector3.h>

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_Vector3__1instanciate(JNIEnv *, jclass, jdouble x, jdouble y, jdouble z)
{
    Ogre::Vector3* r = new Ogre::Vector3(x, y, z);
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT jdouble JNICALL Java_vib_auxiliary_player_ogre_natives_Vector3__1getx(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Vector3* cpp_this = reinterpret_cast<Ogre::Vector3*>(thisPointer);
    return cpp_this->x;
}

JNIEXPORT jdouble JNICALL Java_vib_auxiliary_player_ogre_natives_Vector3__1gety(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Vector3* cpp_this = reinterpret_cast<Ogre::Vector3*>(thisPointer);
    return cpp_this->y;
}

JNIEXPORT jdouble JNICALL Java_vib_auxiliary_player_ogre_natives_Vector3__1getz(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Vector3* cpp_this = reinterpret_cast<Ogre::Vector3*>(thisPointer);
    return cpp_this->z;
}

JNIEXPORT jfloat JNICALL Java_vib_auxiliary_player_ogre_natives_Vector3__1distance(JNIEnv *, jobject, jlong thisPointer, jlong vp)
{
    Ogre::Vector3* cpp_this = reinterpret_cast<Ogre::Vector3*>(thisPointer);
    Ogre::Vector3* v = reinterpret_cast<Ogre::Vector3*>(vp);
    return cpp_this->distance(*v);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_Vector3__1operatorMultiplyAndAssign(JNIEnv *, jobject, jlong thisPointer, jfloat f)
{
    //Ogre::Vector3* r = new Ogre::Vector3((*cpp_this) * f);
    //return reinterpret_cast<jlong>(r);
    Ogre::Vector3* cpp_this = reinterpret_cast<Ogre::Vector3*>(thisPointer);
    *cpp_this = (*cpp_this) * f;
    return thisPointer;
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Vector3_delete(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Vector3* cpp_this = reinterpret_cast<Ogre::Vector3*>(thisPointer);
    delete cpp_this;
}
