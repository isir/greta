#include "RaySceneQueryResult.h"
#include <OgreSceneQuery.h>

JNIEXPORT jint JNICALL Java_vib_auxiliary_player_ogre_natives_RaySceneQueryResult__1size(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::RaySceneQueryResult* cpp_this = reinterpret_cast<Ogre::RaySceneQueryResult*>(thisPointer);
    return cpp_this->size();
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_RaySceneQueryResult__1at(JNIEnv *, jobject, jlong thisPointer, jint i)
{
    Ogre::RaySceneQueryResult* cpp_this = reinterpret_cast<Ogre::RaySceneQueryResult*>(thisPointer);
    Ogre::RaySceneQueryResultEntry* r = &cpp_this->at(i);
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_RaySceneQueryResult_delete(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::RaySceneQueryResult* cpp_this = reinterpret_cast<Ogre::RaySceneQueryResult*>(thisPointer);
    delete cpp_this;
}
