#include "Light.h"
#include <OgreLight.h>

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Light__1setType_1LT_1SPOTLIGHT(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Light* cpp_this = reinterpret_cast<Ogre::Light*>(thisPointer);
    cpp_this->setType(Ogre::Light::LightTypes::LT_SPOTLIGHT);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Light__1setType_1LT_1DIRECTIONAL(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Light* cpp_this = reinterpret_cast<Ogre::Light*>(thisPointer);
    cpp_this->setType(Ogre::Light::LightTypes::LT_DIRECTIONAL);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Light__1detatchFromParent(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Light* cpp_this = reinterpret_cast<Ogre::Light*>(thisPointer);
    cpp_this->detachFromParent();
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Light__1setSpotlightOuterAngle(JNIEnv *, jobject, jlong thisPointer, jdouble d)
{
    Ogre::Light* cpp_this = reinterpret_cast<Ogre::Light*>(thisPointer);
    cpp_this->setSpotlightOuterAngle(Ogre::Radian(d));
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Light__1setDirection(JNIEnv *, jobject, jlong thisPointer, jdouble x, jdouble y, jdouble z)
{
    Ogre::Light* cpp_this = reinterpret_cast<Ogre::Light*>(thisPointer);
    cpp_this->setDirection(x, y, z);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Light__1setCastShadows(JNIEnv *, jobject, jlong thisPointer, jboolean b)
{
    Ogre::Light* cpp_this = reinterpret_cast<Ogre::Light*>(thisPointer);
    cpp_this->setCastShadows(b);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Light__1setDiffuseColour(JNIEnv *, jobject, jlong thisPointer, jlong op)
{
    Ogre::Light* cpp_this = reinterpret_cast<Ogre::Light*>(thisPointer);
    Ogre::ColourValue* o = reinterpret_cast<Ogre::ColourValue*>(op);
    cpp_this->setDiffuseColour(*o);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Light__1setSpecularColour(JNIEnv *, jobject, jlong thisPointer, jlong op)
{
    Ogre::Light* cpp_this = reinterpret_cast<Ogre::Light*>(thisPointer);
    Ogre::ColourValue* o = reinterpret_cast<Ogre::ColourValue*>(op);
    cpp_this->setSpecularColour(*o);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Light_delete(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Light* cpp_this = reinterpret_cast<Ogre::Light*>(thisPointer);
    delete cpp_this;
}
