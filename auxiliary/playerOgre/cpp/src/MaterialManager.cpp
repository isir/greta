#include "MaterialManager.h"
#include <OgreMaterialManager.h>
#include "_Object_.h"

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_MaterialManager__1getSingleton(JNIEnv *, jclass)
{
    Ogre::MaterialManager * r = Ogre::MaterialManager::getSingletonPtr();
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_MaterialManager__1getByName(JNIEnv *env, jobject, jlong thisPointer, jstring s)
{
    Ogre::MaterialManager* cpp_this = reinterpret_cast<Ogre::MaterialManager*>(thisPointer);
    Ogre::String os = ""; convertJStringToOgreString(env, s, os);
    Ogre::Material * r = cpp_this->getByName(os).getPointer();
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT jboolean JNICALL Java_vib_auxiliary_player_ogre_natives_MaterialManager__1resourceExists(JNIEnv *env, jobject, jlong thisPointer, jstring s)
{
    Ogre::MaterialManager* cpp_this = reinterpret_cast<Ogre::MaterialManager*>(thisPointer);
    Ogre::String os = ""; convertJStringToOgreString(env, s, os);
    return cpp_this->resourceExists(os);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_MaterialManager_delete(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::MaterialManager* cpp_this = reinterpret_cast<Ogre::MaterialManager*>(thisPointer);
    delete cpp_this;
}
