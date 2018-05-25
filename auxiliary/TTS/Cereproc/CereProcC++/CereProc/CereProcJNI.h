
#include "stdafx.h"
#include <jni.h>

#ifndef _Included_vib_auxiliary_tts_cereproc_nativetts_CereprocTTS
#define _Included_vib_auxiliary_tts_cereproc_nativetts_CereprocTTS
#ifdef __cplusplus
extern "C" {
#endif

	JNIEXPORT jlong JNICALL Java_vib_auxiliary_tts_cereproc_nativetts_CereprocTTS_initCere
	(JNIEnv *, jclass, jstring, jstring, jstring);

    JNIEXPORT jobjectArray JNICALL Java_vib_auxiliary_tts_cereproc_nativetts_CereprocTTS_speak
    (JNIEnv *, jobject, jstring, jstring, jlong);


#ifdef __cplusplus
}
#endif
#endif
