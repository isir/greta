#include"Pass.h"
#include <OgrePass.h>

JNIEXPORT jboolean JNICALL Java_vib_auxiliary_player_ogre_natives_Pass__1hasFragmentProgram(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Pass* cpp_this = reinterpret_cast<Ogre::Pass*>(thisPointer);
    return cpp_this->hasFragmentProgram();
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_Pass__1getFragmentProgramParameters(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Pass* cpp_this = reinterpret_cast<Ogre::Pass*>(thisPointer);
    Ogre::GpuProgramParameters * r = cpp_this->getFragmentProgramParameters().getPointer();
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Pass__1setAmbient(JNIEnv *, jobject, jlong thisPointer, jlong op)
{
    Ogre::Pass* cpp_this = reinterpret_cast<Ogre::Pass*>(thisPointer);
    Ogre::ColourValue* o = reinterpret_cast<Ogre::ColourValue*>(op);
    cpp_this->setAmbient(*o);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Pass_delete(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Pass* cpp_this = reinterpret_cast<Ogre::Pass*>(thisPointer);
    delete cpp_this;
}
