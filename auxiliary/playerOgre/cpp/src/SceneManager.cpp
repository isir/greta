#include "SceneManager.h"
#include <OgreSceneManager.h>
#include "_Object_.h"

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_SceneManager__1createEntity_1PT_1SPHERE(JNIEnv *env, jobject, jlong thisPointer, jstring id)
{
    Ogre::SceneManager* cpp_this = reinterpret_cast<Ogre::SceneManager*>(thisPointer);
    Ogre::String oid = ""; convertJStringToOgreString(env, id, oid);
    Ogre::Entity* e = cpp_this->createEntity(oid, Ogre::SceneManager::PrefabType::PT_SPHERE);
    return reinterpret_cast<jlong>(e);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_SceneManager__1createEntity_1PT_1CUBE(JNIEnv *env, jobject, jlong thisPointer, jstring id)
{
    Ogre::SceneManager* cpp_this = reinterpret_cast<Ogre::SceneManager*>(thisPointer);
    Ogre::String oid = ""; convertJStringToOgreString(env, id, oid);
    Ogre::Entity* e = cpp_this->createEntity(oid, Ogre::SceneManager::PrefabType::PT_CUBE);
    return reinterpret_cast<jlong>(e);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_SceneManager__1destroyMovableObject(JNIEnv *, jobject, jlong thisPointer, jlong mop)
{
    Ogre::SceneManager* cpp_this = reinterpret_cast<Ogre::SceneManager*>(thisPointer);
    Ogre::MovableObject* mo = reinterpret_cast<Ogre::MovableObject*>(mop);
    cpp_this->destroyMovableObject(mo);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_SceneManager__1setFog_1FOG_1EXP(JNIEnv *, jobject, jlong thisPointer, jlong colourp, jdouble d1, jdouble d2, jdouble d3)
{
    Ogre::SceneManager* cpp_this = reinterpret_cast<Ogre::SceneManager*>(thisPointer);
    Ogre::ColourValue* colour = reinterpret_cast<Ogre::ColourValue*>(colourp);
    cpp_this->setFog(Ogre::FogMode::FOG_EXP, *colour, d1, d2, d3);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_SceneManager__1createRayQuery(JNIEnv *, jobject, jlong thisPointer, jlong rayp, jint i)
{
    Ogre::SceneManager* cpp_this = reinterpret_cast<Ogre::SceneManager*>(thisPointer);
    Ogre::Ray* ray = reinterpret_cast<Ogre::Ray*>(rayp);
    Ogre::RaySceneQuery* rsq = cpp_this->createRayQuery(*ray, ((Ogre::uint32)i));
    return reinterpret_cast<jlong>(rsq);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_SceneManager__1getRootSceneNode(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::SceneManager* cpp_this = reinterpret_cast<Ogre::SceneManager*>(thisPointer);
    Ogre::SceneNode* s = cpp_this->getRootSceneNode();
    return reinterpret_cast<jlong>(s);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_SceneManager__1createCamera(JNIEnv *env, jobject, jlong thisPointer, jstring id)
{
    Ogre::SceneManager* cpp_this = reinterpret_cast<Ogre::SceneManager*>(thisPointer);
    Ogre::String oid = ""; convertJStringToOgreString(env, id, oid);
    Ogre::Camera* c = cpp_this->createCamera(oid);
    return reinterpret_cast<jlong>(c);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_SceneManager__1showBoundingBoxes(JNIEnv *, jobject, jlong thisPointer, jboolean b)
{
    Ogre::SceneManager* cpp_this = reinterpret_cast<Ogre::SceneManager*>(thisPointer);
    cpp_this->showBoundingBoxes(b);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_SceneManager__1createEntity(JNIEnv *env, jobject, jlong thisPointer, jstring id, jstring mesh)
{
    Ogre::SceneManager* cpp_this = reinterpret_cast<Ogre::SceneManager*>(thisPointer);
    Ogre::String oid = ""; convertJStringToOgreString(env, id, oid);
    Ogre::String omesh = ""; convertJStringToOgreString(env, mesh, omesh);
    Ogre::Entity* e = cpp_this->createEntity(oid, omesh);
    return reinterpret_cast<jlong>(e);
}

JNIEXPORT jboolean JNICALL Java_vib_auxiliary_player_ogre_natives_SceneManager__1hasSceneNode(JNIEnv *env, jobject, jlong thisPointer, jstring id)
{
    Ogre::SceneManager* cpp_this = reinterpret_cast<Ogre::SceneManager*>(thisPointer);
    Ogre::String oid = ""; convertJStringToOgreString(env, id, oid);
    return cpp_this->hasSceneNode(oid);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_SceneManager__1getSceneNode(JNIEnv *env, jobject, jlong thisPointer, jstring id)
{
    Ogre::SceneManager* cpp_this = reinterpret_cast<Ogre::SceneManager*>(thisPointer);
    Ogre::String oid = ""; convertJStringToOgreString(env, id, oid);
    Ogre::SceneNode* s = cpp_this->getSceneNode(oid);
    return reinterpret_cast<jlong>(s);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_SceneManager__1getEntity(JNIEnv *env, jobject, jlong thisPointer, jstring id)
{
    Ogre::SceneManager* cpp_this = reinterpret_cast<Ogre::SceneManager*>(thisPointer);
    Ogre::String oid = ""; convertJStringToOgreString(env, id, oid);
    Ogre::Entity* e = cpp_this->getEntity(oid);
    return reinterpret_cast<jlong>(e);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_SceneManager__1createLight(JNIEnv *env, jobject, jlong thisPointer, jstring id)
{
    Ogre::SceneManager* cpp_this = reinterpret_cast<Ogre::SceneManager*>(thisPointer);
    Ogre::String oid = ""; convertJStringToOgreString(env, id, oid);
    Ogre::Light* l = cpp_this->createLight(oid);
    return reinterpret_cast<jlong>(l);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_SceneManager__1getLight(JNIEnv *env, jobject, jlong thisPointer, jstring id)
{
    Ogre::SceneManager* cpp_this = reinterpret_cast<Ogre::SceneManager*>(thisPointer);
    Ogre::String oid = ""; convertJStringToOgreString(env, id, oid);
    Ogre::Light* l = cpp_this->getLight(oid);
    return reinterpret_cast<jlong>(l);
}

JNIEXPORT jboolean JNICALL Java_vib_auxiliary_player_ogre_natives_SceneManager__1hasLight(JNIEnv *env, jobject, jlong thisPointer, jstring id)
{
    Ogre::SceneManager* cpp_this = reinterpret_cast<Ogre::SceneManager*>(thisPointer);
    Ogre::String oid = ""; convertJStringToOgreString(env, id, oid);
    return cpp_this->hasLight(oid);
}

JNIEXPORT jboolean JNICALL Java_vib_auxiliary_player_ogre_natives_SceneManager__1hasEntity(JNIEnv *env, jobject, jlong thisPointer, jstring id)
{
    Ogre::SceneManager* cpp_this = reinterpret_cast<Ogre::SceneManager*>(thisPointer);
    Ogre::String oid = ""; convertJStringToOgreString(env, id, oid);
    return cpp_this->hasEntity(oid);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_SceneManager__1setAmbientLight(JNIEnv *, jobject, jlong thisPointer, jlong colourp)
{
    Ogre::SceneManager* cpp_this = reinterpret_cast<Ogre::SceneManager*>(thisPointer);
    Ogre::ColourValue* colour = reinterpret_cast<Ogre::ColourValue*>(colourp);
    cpp_this->setAmbientLight(*colour);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_SceneManager_delete(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::SceneManager* cpp_this = reinterpret_cast<Ogre::SceneManager*>(thisPointer);
    delete cpp_this;
}
