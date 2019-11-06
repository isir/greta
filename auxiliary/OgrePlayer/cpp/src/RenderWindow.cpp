#include "RenderWindow.h"
#include <OgreRenderWindow.h>

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_RenderWindow__1resize(JNIEnv *, jobject, jlong thisPointer, jlong w, jlong h)
{
    Ogre::RenderWindow* cpp_this = reinterpret_cast<Ogre::RenderWindow*>(thisPointer);
    cpp_this->resize(w, h);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_RenderWindow__1windowMovedOrResized(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::RenderWindow* cpp_this = reinterpret_cast<Ogre::RenderWindow*>(thisPointer);
    cpp_this->windowMovedOrResized();
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_RenderWindow__1addViewport(JNIEnv *, jobject, jlong thisPointer, jlong op, jint i, jdouble d1, jdouble d2, jdouble d3, jdouble d4)
{
    Ogre::RenderWindow* cpp_this = reinterpret_cast<Ogre::RenderWindow*>(thisPointer);
    Ogre::Camera* o = reinterpret_cast<Ogre::Camera*>(op);
    cpp_this->addViewport(o, i, d1, d2, d3, d4);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_RenderWindow__1setVisible(JNIEnv *, jobject, jlong thisPointer, jboolean b)
{
    Ogre::RenderWindow* cpp_this = reinterpret_cast<Ogre::RenderWindow*>(thisPointer);
    cpp_this->setVisible(b);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_RenderWindow__1setAutoUpdated(JNIEnv *, jobject, jlong thisPointer, jboolean b)
{
    Ogre::RenderWindow* cpp_this = reinterpret_cast<Ogre::RenderWindow*>(thisPointer);
    cpp_this->setAutoUpdated(b);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_RenderWindow__1setActive(JNIEnv *, jobject, jlong thisPointer, jboolean b)
{
    Ogre::RenderWindow* cpp_this = reinterpret_cast<Ogre::RenderWindow*>(thisPointer);
    cpp_this->setActive(b);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_RenderWindow__1copyContentsToMemory(JNIEnv *, jobject, jlong thisPointer, jlong op)
{
    Ogre::RenderWindow* cpp_this = reinterpret_cast<Ogre::RenderWindow*>(thisPointer);
    Ogre::PixelBox* o = reinterpret_cast<Ogre::PixelBox*>(op);
    cpp_this->copyContentsToMemory(*o);
}

JNIEXPORT jboolean JNICALL Java_vib_auxiliary_player_ogre_natives_RenderWindow__1isPrimary(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::RenderWindow* cpp_this = reinterpret_cast<Ogre::RenderWindow*>(thisPointer);
    return cpp_this->isPrimary();
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_RenderWindow_delete(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::RenderWindow* cpp_this = reinterpret_cast<Ogre::RenderWindow*>(thisPointer);
    delete cpp_this;
}
