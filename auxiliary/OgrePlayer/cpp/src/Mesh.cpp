#include "Mesh.h"
#include <OgreMesh.h>
#include "_Object_.h"

JNIEXPORT jint JNICALL Java_vib_auxiliary_player_ogre_natives_Mesh__1getPoseCount(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Mesh* cpp_this = reinterpret_cast<Ogre::Mesh*>(thisPointer);
    return cpp_this->getPoseCount();
}

JNIEXPORT jstring JNICALL Java_vib_auxiliary_player_ogre_natives_Mesh__1getName(JNIEnv *env, jobject, jlong thisPointer)
{
    Ogre::Mesh* cpp_this = reinterpret_cast<Ogre::Mesh*>(thisPointer);
    return convertOgreStringToJString(env, cpp_this->getName());
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_Mesh__1getsharedVertexData(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Mesh* cpp_this = reinterpret_cast<Ogre::Mesh*>(thisPointer);
    Ogre::VertexData* r = cpp_this->sharedVertexData;
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_Mesh__1getPose(JNIEnv *, jobject, jlong thisPointer, jint index)
{
    Ogre::Mesh* cpp_this = reinterpret_cast<Ogre::Mesh*>(thisPointer);
    Ogre::Pose* r = cpp_this->getPose(index);
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT jboolean JNICALL Java_vib_auxiliary_player_ogre_natives_Mesh__1hasAnimation(JNIEnv *env, jobject, jlong thisPointer, jstring id)
{
    Ogre::Mesh* cpp_this = reinterpret_cast<Ogre::Mesh*>(thisPointer);
    Ogre::String oid = ""; convertJStringToOgreString(env, id, oid);
    return cpp_this->hasAnimation(oid);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_Mesh__1createAnimation(JNIEnv *env, jobject, jlong thisPointer, jstring id, jint i)
{
    Ogre::Mesh* cpp_this = reinterpret_cast<Ogre::Mesh*>(thisPointer);
    Ogre::String oid = ""; convertJStringToOgreString(env, id, oid);
    Ogre::Animation* r = cpp_this->createAnimation(oid, i);
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_Mesh__1getAnimation(JNIEnv *env, jobject, jlong thisPointer, jstring id)
{
    Ogre::Mesh* cpp_this = reinterpret_cast<Ogre::Mesh*>(thisPointer);
    Ogre::String oid = ""; convertJStringToOgreString(env, id, oid);
    Ogre::Animation* r = cpp_this->getAnimation(oid);
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_Mesh__1getSubMesh(JNIEnv *, jobject, jlong thisPointer, jint index)
{
    Ogre::Mesh* cpp_this = reinterpret_cast<Ogre::Mesh*>(thisPointer);
    Ogre::SubMesh* r = cpp_this->getSubMesh(index);
    return reinterpret_cast<jlong>(r);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Mesh_delete(JNIEnv *, jobject, jlong thisPointer)
{
    Ogre::Mesh* cpp_this = reinterpret_cast<Ogre::Mesh*>(thisPointer);
    delete cpp_this;
}
