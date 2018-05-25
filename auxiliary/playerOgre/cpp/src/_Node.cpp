#include "_Node.h"
#include <OgreNode.h>

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives__1Node__1_1update(JNIEnv *, jobject, jlong thisPointer, jboolean b, jboolean b1)
{
    Ogre::Node* cpp_this = reinterpret_cast<Ogre::Node*>(thisPointer);
    cpp_this->_update(b, b1);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives__1Node__1_1getDerivedOrientation(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Node* cpp_this = reinterpret_cast<Ogre::Node*>(thisPointer);
    const Ogre::Quaternion* cpp_return = &cpp_this->_getDerivedOrientation();
    return reinterpret_cast<jlong>(cpp_return);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives__1Node_delete(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Node* cpp_this = reinterpret_cast<Ogre::Node*>(thisPointer);
    delete cpp_this;
}
