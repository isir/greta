
#include "stdafx.h"
#include <jni.h>

#ifndef _Included_vib_auxiliary_tts_cereproc_nativetts_CereProcTTS
#define _Included_vib_auxiliary_tts_cereproc_nativetts_CereProcTTS
#ifdef __cplusplus
extern "C" {
#endif

	JNIEXPORT jlong JNICALL Java_vib_auxiliary_tts_cereproc_nativetts_CereProcTTS_initCere
	(JNIEnv *, jclass, jstring, jstring, jstring);

    JNIEXPORT jobjectArray JNICALL Java_vib_auxiliary_tts_cereproc_nativetts_CereProcTTS_speak
    (JNIEnv *, jobject, jstring, jstring, jlong);


#ifdef __cplusplus
}
#endif
#endif
