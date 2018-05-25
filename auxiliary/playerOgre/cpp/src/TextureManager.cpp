#include "TextureManager.h"
#include <OgreTextureManager.h>
#include "_Object_.h"

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_TextureManager__1getSingleton(JNIEnv *, jclass)
{
    Ogre::TextureManager* r = Ogre::TextureManager::getSingletonPtr();
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_TextureManager__1remove(JNIEnv *env, jobject, jlong thisPointer, jstring s)
{
    Ogre::TextureManager* cpp_this = reinterpret_cast<Ogre::TextureManager*>(thisPointer);
    Ogre::String os = ""; convertJStringToOgreString(env, s, os);
    cpp_this->remove(os);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_TextureManager__1createRenderTexture(JNIEnv *env, jobject, jlong thisPointer, jstring s, jint w, jint h)
{
    Ogre::TextureManager* cpp_this = reinterpret_cast<Ogre::TextureManager*>(thisPointer);
    Ogre::String os = ""; convertJStringToOgreString(env, s, os);
    Ogre::Texture * r = cpp_this->createManual(os, Ogre::ResourceGroupManager::DEFAULT_RESOURCE_GROUP_NAME, Ogre::TextureType::TEX_TYPE_2D, w, h, 1, 0, Ogre::PixelFormat::PF_BYTE_RGB, Ogre::TextureUsage::TU_RENDERTARGET, 0, false, 4).getPointer();
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_TextureManager_delete(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::TextureManager* cpp_this = reinterpret_cast<Ogre::TextureManager*>(thisPointer);
    delete cpp_this;
}
