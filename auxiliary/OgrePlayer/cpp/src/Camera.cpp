#include "Camera.h"
#include <OgreCamera.h>
#include "_Object_.h"

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Camera__1setPolygonMode_1PM_1SOLID(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Camera* cpp_this = reinterpret_cast<Ogre::Camera*>(thisPointer);
    cpp_this->setPolygonMode(Ogre::PolygonMode::PM_SOLID);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Camera__1setPolygonMode_1PM_1WIREFRAME(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Camera* cpp_this = reinterpret_cast<Ogre::Camera*>(thisPointer);
    cpp_this->setPolygonMode(Ogre::PolygonMode::PM_WIREFRAME);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Camera__1setPolygonMode_1PM_1POINTS(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Camera* cpp_this = reinterpret_cast<Ogre::Camera*>(thisPointer);
    cpp_this->setPolygonMode(Ogre::PolygonMode::PM_POINTS);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Camera__1detatchFromParent(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Camera* cpp_this = reinterpret_cast<Ogre::Camera*>(thisPointer);
    cpp_this->detachFromParent();
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Camera__1setOrthoWindow(JNIEnv *, jobject, jlong thisPointer, jdouble w, jdouble h)
{
    Ogre::Camera* cpp_this = reinterpret_cast<Ogre::Camera*>(thisPointer);
    cpp_this->setOrthoWindow(w, h);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Camera__1setVisible(JNIEnv *, jobject, jlong thisPointer, jboolean b)
{
    Ogre::Camera* cpp_this = reinterpret_cast<Ogre::Camera*>(thisPointer);
    cpp_this->setVisible(b);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Camera__1setDebugDisplayEnabled(JNIEnv *, jobject, jlong thisPointer, jboolean b)
{
    Ogre::Camera* cpp_this = reinterpret_cast<Ogre::Camera*>(thisPointer);
    cpp_this->setDebugDisplayEnabled(b);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Camera__1setNearClipDistance(JNIEnv *, jobject, jlong thisPointer, jdouble d)
{
    Ogre::Camera* cpp_this = reinterpret_cast<Ogre::Camera*>(thisPointer);
    cpp_this->setNearClipDistance(d);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Camera__1setFOVy(JNIEnv *, jobject, jlong thisPointer, jdouble d)
{
    Ogre::Camera* cpp_this = reinterpret_cast<Ogre::Camera*>(thisPointer);
    cpp_this->setFOVy(Ogre::Radian(d));
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Camera__1setCastShadows(JNIEnv *, jobject, jlong thisPointer, jboolean b)
{
    Ogre::Camera* cpp_this = reinterpret_cast<Ogre::Camera*>(thisPointer);
    cpp_this->setCastShadows(b);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Camera__1setAspectRatio(JNIEnv *, jobject, jlong thisPointer, jdouble d)
{
    Ogre::Camera* cpp_this = reinterpret_cast<Ogre::Camera*>(thisPointer);
    cpp_this->setAspectRatio(d);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_Camera__1getSceneManager(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Camera* cpp_this = reinterpret_cast<Ogre::Camera*>(thisPointer);
    Ogre::SceneManager* r = cpp_this->getSceneManager();
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_Camera__1getDerivedOrientation(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Camera* cpp_this = reinterpret_cast<Ogre::Camera*>(thisPointer);
    const Ogre::Quaternion* r = &cpp_this->getDerivedOrientation();
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_Camera__1getDerivedPosition(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Camera* cpp_this = reinterpret_cast<Ogre::Camera*>(thisPointer);
    const Ogre::Vector3* r = &cpp_this->getDerivedPosition();
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_Camera__1getDerivedDirection(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Camera* cpp_this = reinterpret_cast<Ogre::Camera*>(thisPointer);
    Ogre::Vector3* r = new Ogre::Vector3(cpp_this->getDerivedDirection());
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_Camera__1getParentSceneNode(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Camera* cpp_this = reinterpret_cast<Ogre::Camera*>(thisPointer);
    Ogre::SceneNode* r = cpp_this->getParentSceneNode();
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_Camera__1getViewport(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Camera* cpp_this = reinterpret_cast<Ogre::Camera*>(thisPointer);
    Ogre::Viewport* r = cpp_this->getViewport();
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_Camera__1getCameraToViewportRay(JNIEnv *, jobject, jlong thisPointer, jdouble f1, jdouble f2)
{
    Ogre::Camera* cpp_this = reinterpret_cast<Ogre::Camera*>(thisPointer);
    Ogre::Ray* ray = new Ogre::Ray();
    cpp_this->getCameraToViewportRay(f1, f2, ray);
    return reinterpret_cast<jlong>(ray);
}

JNIEXPORT jstring JNICALL Java_vib_auxiliary_player_ogre_natives_Camera__1getName(JNIEnv *env, jobject, jlong thisPointer)
{
    Ogre::Camera* cpp_this = reinterpret_cast<Ogre::Camera*>(thisPointer);
    return convertOgreStringToJString(env, cpp_this->getName());
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Camera_delete(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Camera* cpp_this = reinterpret_cast<Ogre::Camera*>(thisPointer);
    delete cpp_this;
}
