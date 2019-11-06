#include "SubEntity.h"
#include <OgreSubEntity.h>
#include "_Object_.h"

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_SubEntity__1setVisible(JNIEnv *, jobject, jlong thisPointer, jboolean b)
{
    Ogre::SubEntity* cpp_this = reinterpret_cast<Ogre::SubEntity*>(thisPointer);
    cpp_this->setVisible(b);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_SubEntity__1setMaterialName(JNIEnv *env, jobject, jlong thisPointer, jstring s)
{
    Ogre::SubEntity* cpp_this = reinterpret_cast<Ogre::SubEntity*>(thisPointer);
    Ogre::String os = ""; convertJStringToOgreString(env, s, os);
    cpp_this->setMaterialName(os);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_SubEntity_delete(JNIEnv *, jobject, jlong thisPointer)
{
    //Ogre::SubEntity::~SubEntity() is protected
    //Ogre::SubEntity* cpp_this = reinterpret_cast<Ogre::SubEntity*>(thisPointer);
    //delete cpp_this;
}
