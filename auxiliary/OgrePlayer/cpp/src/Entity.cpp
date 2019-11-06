#include "Entity.h"
#include <OgreEntity.h>
#include "_Object_.h"

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Entity__1setVisible(JNIEnv *, jobject, jlong thisPointer, jboolean b)
{
    Ogre::Entity* cpp_this = reinterpret_cast<Ogre::Entity*>(thisPointer);
    cpp_this->setVisible(b);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Entity__1setMaterialName(JNIEnv *env, jobject, jlong thisPointer, jstring id)
{
    Ogre::Entity* cpp_this = reinterpret_cast<Ogre::Entity*>(thisPointer);
    Ogre::String oid = ""; convertJStringToOgreString(env, id, oid);
    cpp_this->setMaterialName(oid);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Entity__1setCastShadows(JNIEnv *, jobject, jlong thisPointer, jboolean b)
{
    Ogre::Entity* cpp_this = reinterpret_cast<Ogre::Entity*>(thisPointer);
    cpp_this->setCastShadows(b);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Entity__1detatchFromParent(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Entity* cpp_this = reinterpret_cast<Ogre::Entity*>(thisPointer);
    cpp_this->detachFromParent();
}

JNIEXPORT jint JNICALL Java_vib_auxiliary_player_ogre_natives_Entity__1getNumSubEntities(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Entity* cpp_this = reinterpret_cast<Ogre::Entity*>(thisPointer);
    return cpp_this->getNumSubEntities();
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_Entity__1_1getManager(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Entity* cpp_this = reinterpret_cast<Ogre::Entity*>(thisPointer);
    Ogre::SceneManager* r = cpp_this->_getManager();
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_Entity__1SkeletonInstance(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Entity* cpp_this = reinterpret_cast<Ogre::Entity*>(thisPointer);
    Ogre::SkeletonInstance* r = cpp_this->getSkeleton();
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_Entity__1getMesh(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Entity* cpp_this = reinterpret_cast<Ogre::Entity*>(thisPointer);
    Ogre::Mesh* r = cpp_this->getMesh().getPointer();
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_Entity__1attachObjectToBone(JNIEnv *env, jobject, jlong thisPointer, jstring id, jlong op)
{
    Ogre::Entity* cpp_this = reinterpret_cast<Ogre::Entity*>(thisPointer);
    Ogre::MovableObject* o = reinterpret_cast<Ogre::MovableObject*>(op);
    Ogre::String oid = ""; convertJStringToOgreString(env, id, oid);
    Ogre::TagPoint* r = cpp_this->attachObjectToBone(oid, o);
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_Entity__1getSubEntity(JNIEnv *, jobject, jlong thisPointer, jint index)
{
    Ogre::Entity* cpp_this = reinterpret_cast<Ogre::Entity*>(thisPointer);
    Ogre::SubEntity* r = cpp_this->getSubEntity(index);
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_Entity__1getWorldBoundingBox(JNIEnv *, jobject, jlong thisPointer, jboolean b)
{
    Ogre::Entity* cpp_this = reinterpret_cast<Ogre::Entity*>(thisPointer);
    const Ogre::AxisAlignedBox* r = &cpp_this->getWorldBoundingBox(b);
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_Entity__1getAnimationState(JNIEnv *env, jobject, jlong thisPointer, jstring id)
{
    Ogre::Entity* cpp_this = reinterpret_cast<Ogre::Entity*>(thisPointer);
    Ogre::String oid = ""; convertJStringToOgreString(env, id, oid);
    Ogre::AnimationState* r = cpp_this->getAnimationState(oid);
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT jstring JNICALL Java_vib_auxiliary_player_ogre_natives_Entity__1getName(JNIEnv *env, jobject, jlong thisPointer)
{
    Ogre::Entity* cpp_this = reinterpret_cast<Ogre::Entity*>(thisPointer);
    return convertOgreStringToJString(env, cpp_this->getName());
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Entity_delete(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Entity* cpp_this = reinterpret_cast<Ogre::Entity*>(thisPointer);
    delete cpp_this;
}
