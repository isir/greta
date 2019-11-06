#include "SubMesh.h"
#include <OgreSubMesh.h>

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_SubMesh__1getvertexData(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::SubMesh* cpp_this = reinterpret_cast<Ogre::SubMesh*>(thisPointer);
    Ogre::VertexData* r = cpp_this->vertexData;
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT jboolean JNICALL Java_vib_auxiliary_player_ogre_natives_SubMesh__1getuseSharedVertices(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::SubMesh* cpp_this = reinterpret_cast<Ogre::SubMesh*>(thisPointer);
    return cpp_this->useSharedVertices;
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_SubMesh_delete(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::SubMesh* cpp_this = reinterpret_cast<Ogre::SubMesh*>(thisPointer);
    delete cpp_this;
}
