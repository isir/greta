#include "RaySceneQueryResultEntry.h"
#include <OgreSceneQuery.h>

JNIEXPORT jdouble JNICALL Java_vib_auxiliary_player_ogre_natives_RaySceneQueryResultEntry__1distance(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::RaySceneQueryResultEntry* cpp_this = reinterpret_cast<Ogre::RaySceneQueryResultEntry*>(thisPointer);
    return cpp_this->distance;
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_RaySceneQueryResultEntry_delete(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::RaySceneQueryResultEntry* cpp_this = reinterpret_cast<Ogre::RaySceneQueryResultEntry*>(thisPointer);
    delete cpp_this;
}
