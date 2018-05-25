#include "NameValuePairList.h"
#include <OgreCommon.h>
#include "_Object_.h"

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_NameValuePairList__1instanciate(JNIEnv *, jclass)
{
    Ogre::NameValuePairList * r = new Ogre::NameValuePairList();
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_NameValuePairList__1insert(JNIEnv *env, jobject, jlong thisPointer, jstring key, jstring val)
{
    Ogre::NameValuePairList* cpp_this = reinterpret_cast<Ogre::NameValuePairList*>(thisPointer);
    Ogre::String okey = ""; convertJStringToOgreString(env, key, okey);
    Ogre::String oval = ""; convertJStringToOgreString(env, val, oval);
    cpp_this->insert(std::make_pair(okey, oval));
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_NameValuePairList_delete(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::NameValuePairList* cpp_this = reinterpret_cast<Ogre::NameValuePairList*>(thisPointer);
    delete cpp_this;
}
