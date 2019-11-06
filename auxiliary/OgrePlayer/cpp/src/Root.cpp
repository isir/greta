#include "Root.h"
#include <OgreRoot.h>
#include "_Object_.h"

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_Root__1instanciate(JNIEnv *env, jclass, jstring s1, jstring s2, jstring s3)
{
    Ogre::String os1 = ""; convertJStringToOgreString(env, s1, os1);
    Ogre::String os2 = ""; convertJStringToOgreString(env, s2, os2);
    Ogre::String os3 = ""; convertJStringToOgreString(env, s3, os3);
    Ogre::Root * root = new Ogre::Root(os1, os2, os3);
    return reinterpret_cast<jlong>(root);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Root__1shutdown(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Root* cpp_this = reinterpret_cast<Ogre::Root*>(thisPointer);
    cpp_this->shutdown();
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Root__1renderOneFrame(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Root* cpp_this = reinterpret_cast<Ogre::Root*>(thisPointer);
    cpp_this->renderOneFrame();
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Root__1_1fireFrameStarted(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Root* cpp_this = reinterpret_cast<Ogre::Root*>(thisPointer);
    cpp_this->_fireFrameStarted();
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Root__1_1fireFrameEnded(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Root* cpp_this = reinterpret_cast<Ogre::Root*>(thisPointer);
    cpp_this->_fireFrameEnded();
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Root__1setRenderSystem(JNIEnv *env, jobject, jlong thisPointer, jstring s)
{
    Ogre::Root* cpp_this = reinterpret_cast<Ogre::Root*>(thisPointer);
    Ogre::String os = ""; convertJStringToOgreString(env, s, os);
    cpp_this->setRenderSystem(cpp_this->getRenderSystemByName(os));
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Root__1detachRenderTarget(JNIEnv *, jobject, jlong thisPointer, jlong rp)
{
    Ogre::Root* cpp_this = reinterpret_cast<Ogre::Root*>(thisPointer);
    Ogre::RenderTarget* r = reinterpret_cast<Ogre::RenderTarget*>(rp);
    cpp_this->detachRenderTarget(r);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Root__1initialise(JNIEnv *env, jobject, jlong thisPointer, jboolean b, jstring s1, jstring s2)
{
    Ogre::Root* cpp_this = reinterpret_cast<Ogre::Root*>(thisPointer);
    Ogre::String os1 = ""; convertJStringToOgreString(env, s1, os1);
    Ogre::String os2 = ""; convertJStringToOgreString(env, s2, os2);
    cpp_this->initialise(b, os1, os2);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_Root__1createRenderWindow(JNIEnv *env, jobject, jlong thisPointer, jstring s, jint i1, jint i2, jboolean b, jlong op)
{
    Ogre::Root* cpp_this = reinterpret_cast<Ogre::Root*>(thisPointer);
    Ogre::NameValuePairList* o = reinterpret_cast<Ogre::NameValuePairList*>(op);
    Ogre::String os = ""; convertJStringToOgreString(env, s, os);
    Ogre::RenderWindow * r = cpp_this->createRenderWindow(os, i1, i2, b, o);
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_Root__1getSceneManager(JNIEnv *env, jobject, jlong thisPointer, jstring s)
{
    Ogre::Root* cpp_this = reinterpret_cast<Ogre::Root*>(thisPointer);
    Ogre::String os = ""; convertJStringToOgreString(env, s, os);
    Ogre::SceneManager * r = cpp_this->getSceneManager(os);
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_Root__1createSceneManager(JNIEnv *env, jobject, jlong thisPointer, jstring s1, jstring s2)
{
    Ogre::Root* cpp_this = reinterpret_cast<Ogre::Root*>(thisPointer);
    Ogre::String os1 = ""; convertJStringToOgreString(env, s1, os1);
    Ogre::String os2 = ""; convertJStringToOgreString(env, s2, os2);
    Ogre::SceneManager * r = cpp_this->createSceneManager(os1, os2);
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Root_delete(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Root* cpp_this = reinterpret_cast<Ogre::Root*>(thisPointer);
    delete cpp_this;
}
