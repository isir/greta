#include "SceneNode.h"
#include <OgreSceneNode.h>
#include "_Object_.h"

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_SceneNode__1yaw(JNIEnv *, jobject, jlong thisPointer, jdouble d)
{
    Ogre::SceneNode* cpp_this = reinterpret_cast<Ogre::SceneNode*>(thisPointer);
    cpp_this->yaw(Ogre::Radian(d));
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_SceneNode__1pitch(JNIEnv *, jobject, jlong thisPointer, jdouble d)
{
    Ogre::SceneNode* cpp_this = reinterpret_cast<Ogre::SceneNode*>(thisPointer);
    cpp_this->pitch(Ogre::Radian(d));
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_SceneNode__1roll(JNIEnv *, jobject, jlong thisPointer, jdouble d)
{
    Ogre::SceneNode* cpp_this = reinterpret_cast<Ogre::SceneNode*>(thisPointer);
    cpp_this->roll(Ogre::Radian(d));
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_SceneNode__1_1update(JNIEnv *, jobject, jlong thisPointer, jboolean b1, jboolean b2)
{
    Ogre::SceneNode* cpp_this = reinterpret_cast<Ogre::SceneNode*>(thisPointer);
    cpp_this->_update(b1, b2);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_SceneNode__1setPosition__JDDD(JNIEnv *, jobject, jlong thisPointer, jdouble x, jdouble y, jdouble z)
{
    Ogre::SceneNode* cpp_this = reinterpret_cast<Ogre::SceneNode*>(thisPointer);
    cpp_this->setPosition(x, y, z);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_SceneNode__1setScale__JDDD(JNIEnv *, jobject, jlong thisPointer, jdouble x, jdouble y, jdouble z)
{
    Ogre::SceneNode* cpp_this = reinterpret_cast<Ogre::SceneNode*>(thisPointer);
    cpp_this->setScale(x, y, z);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_SceneNode__1removeAllChildren(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::SceneNode* cpp_this = reinterpret_cast<Ogre::SceneNode*>(thisPointer);
    cpp_this->removeAllChildren();
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_SceneNode__1removeAndDestroyAllChildren(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::SceneNode* cpp_this = reinterpret_cast<Ogre::SceneNode*>(thisPointer);
    cpp_this->removeAndDestroyAllChildren();
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_SceneNode__1setVisible(JNIEnv *, jobject, jlong thisPointer, jboolean b1, jboolean b2)
{
    Ogre::SceneNode* cpp_this = reinterpret_cast<Ogre::SceneNode*>(thisPointer);
    cpp_this->setVisible(b1, b2);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_SceneNode__1attachObject(JNIEnv *, jobject, jlong thisPointer, jlong op)
{
    Ogre::SceneNode* cpp_this = reinterpret_cast<Ogre::SceneNode*>(thisPointer);
    Ogre::MovableObject* o = reinterpret_cast<Ogre::MovableObject*>(op);
    cpp_this->attachObject(o);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_SceneNode__1translate(JNIEnv *, jobject, jlong thisPointer, jlong op)
{
    Ogre::SceneNode* cpp_this = reinterpret_cast<Ogre::SceneNode*>(thisPointer);
    Ogre::Vector3* o = reinterpret_cast<Ogre::Vector3*>(op);
    cpp_this->translate(*o, Ogre::Node::TS_LOCAL);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_SceneNode__1setOrientation(JNIEnv *, jobject, jlong thisPointer, jlong op)
{
    Ogre::SceneNode* cpp_this = reinterpret_cast<Ogre::SceneNode*>(thisPointer);
    Ogre::Quaternion* o = reinterpret_cast<Ogre::Quaternion*>(op);
    cpp_this->setOrientation(*o);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_SceneNode__1scale(JNIEnv *, jobject, jlong thisPointer, jlong op)
{
    Ogre::SceneNode* cpp_this = reinterpret_cast<Ogre::SceneNode*>(thisPointer);
    Ogre::Vector3* o = reinterpret_cast<Ogre::Vector3*>(op);
    cpp_this->scale(*o);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_SceneNode__1setPosition__JJ(JNIEnv *, jobject, jlong thisPointer, jlong op)
{
    Ogre::SceneNode* cpp_this = reinterpret_cast<Ogre::SceneNode*>(thisPointer);
    Ogre::Vector3* o = reinterpret_cast<Ogre::Vector3*>(op);
    cpp_this->setPosition(*o);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_SceneNode__1setScale__JJ(JNIEnv *, jobject, jlong thisPointer, jlong op)
{
    Ogre::SceneNode* cpp_this = reinterpret_cast<Ogre::SceneNode*>(thisPointer);
    Ogre::Vector3* o = reinterpret_cast<Ogre::Vector3*>(op);
    cpp_this->setScale(*o);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_SceneNode__1removeChild(JNIEnv *, jobject, jlong thisPointer, jlong op)
{
    Ogre::SceneNode* cpp_this = reinterpret_cast<Ogre::SceneNode*>(thisPointer);
    Ogre::Node* o = reinterpret_cast<Ogre::Node*>(op);
    cpp_this->removeChild(o);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_SceneNode__1addChild(JNIEnv *, jobject, jlong thisPointer, jlong op)
{
    Ogre::SceneNode* cpp_this = reinterpret_cast<Ogre::SceneNode*>(thisPointer);
    Ogre::Node* o = reinterpret_cast<Ogre::Node*>(op);
    cpp_this->addChild(o);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_SceneNode__1_1getDerivedOrientation(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::SceneNode* cpp_this = reinterpret_cast<Ogre::SceneNode*>(thisPointer);
    const Ogre::Quaternion * r = & cpp_this->_getDerivedOrientation();
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_SceneNode__1getOrientation(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::SceneNode* cpp_this = reinterpret_cast<Ogre::SceneNode*>(thisPointer);
    const Ogre::Quaternion * r = &cpp_this->getOrientation();
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_SceneNode__1_1getDerivedPosition(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::SceneNode* cpp_this = reinterpret_cast<Ogre::SceneNode*>(thisPointer);
    const Ogre::Vector3 * r = &cpp_this->_getDerivedPosition();
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_SceneNode__1getScale(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::SceneNode* cpp_this = reinterpret_cast<Ogre::SceneNode*>(thisPointer);
    const Ogre::Vector3 * r = &cpp_this->getScale();
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_SceneNode__1getPosition(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::SceneNode* cpp_this = reinterpret_cast<Ogre::SceneNode*>(thisPointer);
    const Ogre::Vector3 * r = &cpp_this->getPosition();
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_SceneNode__1createChildSceneNode__JLjava_lang_String_2(JNIEnv *env, jobject, jlong thisPointer, jstring id)
{
    Ogre::SceneNode* cpp_this = reinterpret_cast<Ogre::SceneNode*>(thisPointer);
    Ogre::String oid = ""; convertJStringToOgreString(env, id, oid);
    const Ogre::SceneNode * r = cpp_this->createChildSceneNode(oid);
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_SceneNode__1createChildSceneNode__JLjava_lang_String_2J(JNIEnv *env, jobject, jlong thisPointer, jstring id, jlong vp)
{
    Ogre::SceneNode* cpp_this = reinterpret_cast<Ogre::SceneNode*>(thisPointer);
    Ogre::Vector3* v = reinterpret_cast<Ogre::Vector3*>(vp);
    Ogre::String oid = ""; convertJStringToOgreString(env, id, oid);
    const Ogre::SceneNode * r = cpp_this->createChildSceneNode(oid, *v);
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_SceneNode__1getParentSceneNode(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::SceneNode* cpp_this = reinterpret_cast<Ogre::SceneNode*>(thisPointer);
    const Ogre::SceneNode * r = cpp_this->getParentSceneNode();
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_SceneNode__1createChildSceneNode__J(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::SceneNode* cpp_this = reinterpret_cast<Ogre::SceneNode*>(thisPointer);
    const Ogre::SceneNode * r = cpp_this->createChildSceneNode();
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_SceneNode__1getAttachedObject(JNIEnv *, jobject, jlong thisPointer, jint index)
{
    Ogre::SceneNode* cpp_this = reinterpret_cast<Ogre::SceneNode*>(thisPointer);
    const Ogre::MovableObject * r = cpp_this->getAttachedObject(index);
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT jint JNICALL Java_vib_auxiliary_player_ogre_natives_SceneNode__1numChildren(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::SceneNode* cpp_this = reinterpret_cast<Ogre::SceneNode*>(thisPointer);
    return cpp_this->numChildren();
}

JNIEXPORT jint JNICALL Java_vib_auxiliary_player_ogre_natives_SceneNode__1numAttachedObjects(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::SceneNode* cpp_this = reinterpret_cast<Ogre::SceneNode*>(thisPointer);
    return cpp_this->numAttachedObjects();
}

JNIEXPORT jstring JNICALL Java_vib_auxiliary_player_ogre_natives_SceneNode__1getName(JNIEnv *env, jobject, jlong thisPointer)
{
    Ogre::SceneNode* cpp_this = reinterpret_cast<Ogre::SceneNode*>(thisPointer);
    return convertOgreStringToJString(env, cpp_this->getName());
}

JNIEXPORT jstring JNICALL Java_vib_auxiliary_player_ogre_natives_SceneNode__1getChild_1getName(JNIEnv *env, jobject, jlong thisPointer, jint index)
{
    Ogre::SceneNode* cpp_this = reinterpret_cast<Ogre::SceneNode*>(thisPointer);
    return convertOgreStringToJString(env, cpp_this->getChild(index)->getName());
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_SceneNode_delete(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::SceneNode* cpp_this = reinterpret_cast<Ogre::SceneNode*>(thisPointer);
    delete cpp_this;
}
