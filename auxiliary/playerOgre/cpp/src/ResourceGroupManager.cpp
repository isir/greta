#include "ResourceGroupManager.h"
#include <OgreResourceGroupManager.h>
#include "_Object_.h"

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_ResourceGroupManager__1getSingleton(JNIEnv *, jclass)
{
    Ogre::ResourceGroupManager* r = Ogre::ResourceGroupManager::getSingletonPtr();
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT jstring JNICALL Java_vib_auxiliary_player_ogre_natives_ResourceGroupManager_getDEFAULT_1RESOURCE_1GROUP_1NAME(JNIEnv *env, jclass)
{
    return convertOgreStringToJString(env, Ogre::ResourceGroupManager::DEFAULT_RESOURCE_GROUP_NAME);
}

JNIEXPORT jboolean JNICALL Java_vib_auxiliary_player_ogre_natives_ResourceGroupManager__1isResourceGroupInitialised(JNIEnv *env, jobject, jlong thisPointer, jstring s)
{
    Ogre::ResourceGroupManager* cpp_this = reinterpret_cast<Ogre::ResourceGroupManager*>(thisPointer);
    Ogre::String os = ""; convertJStringToOgreString(env, s, os);
    return cpp_this->isResourceGroupInitialised(os);
}

JNIEXPORT jboolean JNICALL Java_vib_auxiliary_player_ogre_natives_ResourceGroupManager__1resourceExists(JNIEnv *env, jobject, jlong thisPointer, jstring s1, jstring s2)
{
    Ogre::ResourceGroupManager* cpp_this = reinterpret_cast<Ogre::ResourceGroupManager*>(thisPointer);
    Ogre::String os1 = ""; convertJStringToOgreString(env, s1, os1);
    Ogre::String os2 = ""; convertJStringToOgreString(env, s2, os2);
    return cpp_this->resourceExists(os1, os2);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_ResourceGroupManager__1addResourceLocation(JNIEnv *env, jobject, jlong thisPointer, jstring s1, jstring s2, jstring s3, jboolean b)
{
    Ogre::ResourceGroupManager* cpp_this = reinterpret_cast<Ogre::ResourceGroupManager*>(thisPointer);
    Ogre::String os1 = ""; convertJStringToOgreString(env, s1, os1);
    Ogre::String os2 = ""; convertJStringToOgreString(env, s2, os2);
    Ogre::String os3 = ""; convertJStringToOgreString(env, s3, os3);
    cpp_this->addResourceLocation(os1, os2, os3, b);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_ResourceGroupManager__1initialiseAllResourceGroups(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::ResourceGroupManager* cpp_this = reinterpret_cast<Ogre::ResourceGroupManager*>(thisPointer);
#if defined (_WIN32)
#else
	char * previousLocale = setlocale (LC_ALL, "C");
#endif
    cpp_this->initialiseAllResourceGroups();
#if defined (_WIN32)
#else
	setlocale (LC_ALL, previousLocale);
#endif
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_ResourceGroupManager_delete(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::ResourceGroupManager* cpp_this = reinterpret_cast<Ogre::ResourceGroupManager*>(thisPointer);
    delete cpp_this;
}
