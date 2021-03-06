/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class vib_auxiliary_player_ogre_natives_Entity */

#ifndef _Included_vib_auxiliary_player_ogre_natives_Entity
#define _Included_vib_auxiliary_player_ogre_natives_Entity
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     vib_auxiliary_player_ogre_natives_Entity
 * Method:    _setVisible
 * Signature: (JZ)V
 */
JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Entity__1setVisible
  (JNIEnv *, jobject, jlong, jboolean);

/*
 * Class:     vib_auxiliary_player_ogre_natives_Entity
 * Method:    _setMaterialName
 * Signature: (JLjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Entity__1setMaterialName
  (JNIEnv *, jobject, jlong, jstring);

/*
 * Class:     vib_auxiliary_player_ogre_natives_Entity
 * Method:    _getNumSubEntities
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_vib_auxiliary_player_ogre_natives_Entity__1getNumSubEntities
  (JNIEnv *, jobject, jlong);

/*
 * Class:     vib_auxiliary_player_ogre_natives_Entity
 * Method:    __getManager
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_Entity__1_1getManager
  (JNIEnv *, jobject, jlong);

/*
 * Class:     vib_auxiliary_player_ogre_natives_Entity
 * Method:    _SkeletonInstance
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_Entity__1SkeletonInstance
  (JNIEnv *, jobject, jlong);

/*
 * Class:     vib_auxiliary_player_ogre_natives_Entity
 * Method:    _getMesh
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_Entity__1getMesh
  (JNIEnv *, jobject, jlong);

/*
 * Class:     vib_auxiliary_player_ogre_natives_Entity
 * Method:    _setCastShadows
 * Signature: (JZ)V
 */
JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Entity__1setCastShadows
  (JNIEnv *, jobject, jlong, jboolean);

/*
 * Class:     vib_auxiliary_player_ogre_natives_Entity
 * Method:    _getName
 * Signature: (J)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_vib_auxiliary_player_ogre_natives_Entity__1getName
  (JNIEnv *, jobject, jlong);

/*
 * Class:     vib_auxiliary_player_ogre_natives_Entity
 * Method:    _detatchFromParent
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Entity__1detatchFromParent
  (JNIEnv *, jobject, jlong);

/*
 * Class:     vib_auxiliary_player_ogre_natives_Entity
 * Method:    _attachObjectToBone
 * Signature: (JLjava/lang/String;J)J
 */
JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_Entity__1attachObjectToBone
  (JNIEnv *, jobject, jlong, jstring, jlong);

/*
 * Class:     vib_auxiliary_player_ogre_natives_Entity
 * Method:    _getSubEntity
 * Signature: (JI)J
 */
JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_Entity__1getSubEntity
  (JNIEnv *, jobject, jlong, jint);

/*
 * Class:     vib_auxiliary_player_ogre_natives_Entity
 * Method:    _getWorldBoundingBox
 * Signature: (JZ)J
 */
JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_Entity__1getWorldBoundingBox
  (JNIEnv *, jobject, jlong, jboolean);

/*
 * Class:     vib_auxiliary_player_ogre_natives_Entity
 * Method:    _getAnimationState
 * Signature: (JLjava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_vib_auxiliary_player_ogre_natives_Entity__1getAnimationState
  (JNIEnv *, jobject, jlong, jstring);

/*
 * Class:     vib_auxiliary_player_ogre_natives_Entity
 * Method:    delete
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_Entity_delete
  (JNIEnv *, jobject, jlong);

#ifdef __cplusplus
}
#endif
#endif
