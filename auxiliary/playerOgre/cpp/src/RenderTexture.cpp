#include "RenderTexture.h"
#include <OgreRenderTexture.h>

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_RenderTexture__1setActive(JNIEnv *, jobject, jlong thisPointer, jboolean b)
{
    Ogre::RenderTexture* cpp_this = reinterpret_cast<Ogre::RenderTexture*>(thisPointer);
    cpp_this->setActive(b);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_RenderTexture__1setAutoUpdated(JNIEnv *, jobject, jlong thisPointer, jboolean b)
{
    Ogre::RenderTexture* cpp_this = reinterpret_cast<Ogre::RenderTexture*>(thisPointer);
    cpp_this->setAutoUpdated(b);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_RenderTexture__1removeAllViewports(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::RenderTexture* cpp_this = reinterpret_cast<Ogre::RenderTexture*>(thisPointer);
    cpp_this->removeAllViewports();
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_RenderTexture__1addViewport(JNIEnv *, jobject, jlong thisPointer, jlong op, jint i, jdouble d1, jdouble d2, jdouble d3, jdouble d4)
{
    Ogre::RenderTexture* cpp_this = reinterpret_cast<Ogre::RenderTexture*>(thisPointer);
    Ogre::Camera* o = reinterpret_cast<Ogre::Camera*>(op);
    cpp_this->addViewport(o, 1, d1, d2, d3, d4);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_RenderTexture_delete(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::RenderTexture* cpp_this = reinterpret_cast<Ogre::RenderTexture*>(thisPointer);
    delete cpp_this;
}
