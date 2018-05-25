#include "RaySceneQuery.h"
#include <OgreSceneQuery.h>

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_RaySceneQuery__1setSortByDistance(JNIEnv *, jobject, jlong thisPointer, jboolean b, jint i)
{
    Ogre::RaySceneQuery* cpp_this = reinterpret_cast<Ogre::RaySceneQuery*>(thisPointer);
    cpp_this->setSortByDistance(b, i);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_RaySceneQuery__1execute(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::RaySceneQuery* cpp_this = reinterpret_cast<Ogre::RaySceneQuery*>(thisPointer);
    Ogre::RaySceneQueryResult * r = &cpp_this->execute();
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_RaySceneQuery_delete(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::RaySceneQuery* cpp_this = reinterpret_cast<Ogre::RaySceneQuery*>(thisPointer);
    delete cpp_this;
}
