#include "VertexDeclaration.h"
#include <OgreHardwareVertexBuffer.h>

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_VertexDeclaration__1getAutoOrganisedDeclaration(JNIEnv *, jobject, jlong thisPointer, jboolean b1, jboolean b2)
{
    Ogre::VertexDeclaration* cpp_this = reinterpret_cast<Ogre::VertexDeclaration*>(thisPointer);
    Ogre::VertexDeclaration* r = cpp_this->getAutoOrganisedDeclaration(b1, b2, false);
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_VertexDeclaration_delete(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::VertexDeclaration* cpp_this = reinterpret_cast<Ogre::VertexDeclaration*>(thisPointer);
    delete cpp_this;
}
