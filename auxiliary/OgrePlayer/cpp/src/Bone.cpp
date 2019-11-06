#include "Bone.h"
#include <OgreBone.h>
#include "_Object_.h"

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Bone__1_1update(JNIEnv *, jobject, jlong thisPointer, jboolean b1, jboolean b2)
{
    Ogre::Bone* cpp_this = reinterpret_cast<Ogre::Bone*>(thisPointer);
    cpp_this->_update(b1, b2);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_Bone__1getPosition(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Bone* cpp_this = reinterpret_cast<Ogre::Bone*>(thisPointer);
    const Ogre::Vector3* v = &cpp_this->getPosition();
    return reinterpret_cast<jlong>(v);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_Bone__1_1getDerivedOrientation(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Bone* cpp_this = reinterpret_cast<Ogre::Bone*>(thisPointer);
    const Ogre::Quaternion* q = &cpp_this->_getDerivedOrientation();
    return reinterpret_cast<jlong>(q);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_Bone__1getOrientation(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Bone* cpp_this = reinterpret_cast<Ogre::Bone*>(thisPointer);
    const Ogre::Quaternion* q = &cpp_this->getOrientation();
    return reinterpret_cast<jlong>(q);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Bone__1setOrientation__JJ(JNIEnv *, jobject, jlong thisPointer, jlong qPointer)
{
    Ogre::Bone* cpp_this = reinterpret_cast<Ogre::Bone*>(thisPointer);
    Ogre::Quaternion* q = reinterpret_cast<Ogre::Quaternion*>(qPointer);
    cpp_this->setOrientation(*q);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Bone__1setOrientation__JDDDD(JNIEnv *, jobject, jlong thisPointer, jdouble w, jdouble x, jdouble y, jdouble z)
{
    Ogre::Bone* cpp_this = reinterpret_cast<Ogre::Bone*>(thisPointer);
    cpp_this->setOrientation(w, x, y, z);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Bone__1setManuallyControlled(JNIEnv *, jobject, jlong thisPointer, jboolean b)
{
    Ogre::Bone* cpp_this = reinterpret_cast<Ogre::Bone*>(thisPointer);
    cpp_this->setManuallyControlled(b);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Bone__1setPosition(JNIEnv *, jobject, jlong thisPointer, jdouble x, jdouble y, jdouble z)
{
    Ogre::Bone* cpp_this = reinterpret_cast<Ogre::Bone*>(thisPointer);
    cpp_this->setPosition(x, y, z);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Bone__1setScale(JNIEnv *, jobject, jlong thisPointer, jdouble x, jdouble y, jdouble z)
{
    Ogre::Bone* cpp_this = reinterpret_cast<Ogre::Bone*>(thisPointer);
    cpp_this->setScale(x, y, z);
}

JNIEXPORT jstring JNICALL Java_vib_auxiliary_player_ogre_natives_Bone__1getName(JNIEnv *env, jobject, jlong thisPointer)
{
    Ogre::Bone* cpp_this = reinterpret_cast<Ogre::Bone*>(thisPointer);
    return convertOgreStringToJString(env, cpp_this->getName());
}

JNIEXPORT jint JNICALL Java_vib_auxiliary_player_ogre_natives_Bone__1numChildren(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Bone* cpp_this = reinterpret_cast<Ogre::Bone*>(thisPointer);
    return cpp_this->numChildren();
}

JNIEXPORT jstring JNICALL Java_vib_auxiliary_player_ogre_natives_Bone__1getChild_1getName(JNIEnv *env, jobject, jlong thisPointer, jint index)
{
    Ogre::Bone* cpp_this = reinterpret_cast<Ogre::Bone*>(thisPointer);
    return convertOgreStringToJString(env, cpp_this->getChild(index)->getName());
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_Bone__1_1getDerivedPosition(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Bone* cpp_this = reinterpret_cast<Ogre::Bone*>(thisPointer);
    const Ogre::Vector3* v = &cpp_this->_getDerivedPosition();
    return reinterpret_cast<jlong>(v);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_Bone__1getParent(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Bone* cpp_this = reinterpret_cast<Ogre::Bone*>(thisPointer);
    Ogre::Node* n = cpp_this->getParent();
    return reinterpret_cast<jlong>(n);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Bone_delete(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Bone* cpp_this = reinterpret_cast<Ogre::Bone*>(thisPointer);
    delete cpp_this;
}
