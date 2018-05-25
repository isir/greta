#include "VertexData.h"
#include <OgreVertexIndexData.h>

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_VertexData__1getvertexDeclaration(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::VertexData* cpp_this = reinterpret_cast<Ogre::VertexData*>(thisPointer);
    Ogre::VertexDeclaration* r = cpp_this->vertexDeclaration;
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_VertexData__1reorganiseBuffers(JNIEnv *, jobject, jlong thisPointer, jlong op)
{
    Ogre::VertexData* cpp_this = reinterpret_cast<Ogre::VertexData*>(thisPointer);
    Ogre::VertexDeclaration* o = reinterpret_cast<Ogre::VertexDeclaration*>(op);
    cpp_this->reorganiseBuffers(o);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_VertexData_delete(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::VertexData* cpp_this = reinterpret_cast<Ogre::VertexData*>(thisPointer);
    delete cpp_this;
}
