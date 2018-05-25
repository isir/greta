#include "MeshManager.h"
#include <OgreMeshManager.h>
#include "_Object_.h"

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_MeshManager__1load(JNIEnv *env, jclass, jstring s1, jstring s2)
{
    Ogre::String os1 = ""; convertJStringToOgreString(env, s1, os1);
    Ogre::String os2 = ""; convertJStringToOgreString(env, s2, os2);
    Ogre::Mesh * r = Ogre::MeshManager::getSingleton().load(os1, os2).getPointer();
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT jfloat JNICALL Java_vib_auxiliary_player_ogre_natives_MeshManager_getBoundsPaddingFactor(JNIEnv *, jclass)
{
    return Ogre::MeshManager::getSingleton().getBoundsPaddingFactor();
}
