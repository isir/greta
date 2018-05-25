#include "_MovableObject.h"
#include <OgreMovableObject.h>

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives__1MovableObject__1detatchFromParent(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::MovableObject* cpp_this = reinterpret_cast<Ogre::MovableObject*>(thisPointer);
    cpp_this->detachFromParent();
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives__1MovableObject_delete(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::MovableObject* cpp_this = reinterpret_cast<Ogre::MovableObject*>(thisPointer);
    delete cpp_this;
}
