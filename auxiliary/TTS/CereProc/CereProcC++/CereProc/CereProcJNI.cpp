
#include "stdafx.h"
#include "CereProcJNI.h"
#include "CereProc.h"
#include <jni.h>
#include <vector>
#include <string.h>

jmethodID printERROR;
jmethodID MID_String_getBytes;
jclass cereprocTTSClass;
jobject cereprocTTSInstance;
JNIEnv * java_env;
JavaVM * jvm;

static CereProc cere;

//call of java function to print to console
void printERR(const char* message) {
    java_env->CallStaticVoidMethod(cereprocTTSClass, printERROR, java_env->NewStringUTF(message));
}

//utility function to convert java strings to c++ char*
char * convertString(JNIEnv *env, jstring jstr) {
    jbyteArray bytes = 0;
    jthrowable exc;
    char *result = 0;
    if (env->EnsureLocalCapacity(2) < 0) {
        return 0; /* out of memory error */
    }
    jobject o;
    bytes = (jbyteArray) (env->CallObjectMethod(jstr, MID_String_getBytes));
    exc = env->ExceptionOccurred();
    if (!exc) {
        jint len = env->GetArrayLength(bytes);
        result = (char *) malloc(len + 1);
        if (result == 0) {
            //JNU_ThrowByName(env, "java/lang/OutOfMemoryError", 0);
            env->DeleteLocalRef(bytes);
            return 0;
        }
        env->GetByteArrayRegion(bytes, 0, len,
                (jbyte *) result);
        result[len] = 0; /* NULL-terminate */
    } else {
        env->DeleteLocalRef(exc);
    }
    env->DeleteLocalRef(bytes);
    return result;
}

//init function
JNIEXPORT jlong JNICALL Java_vib_auxiliary_tts_cereproc_nativetts_CereProcTTS_initCere	(JNIEnv * env, jclass c, jstring lic, jstring voc, jstring aud) {
    java_env = env;

    printERROR = java_env->GetStaticMethodID(c, "printERROR", "(Ljava/lang/String;)V");
	MID_String_getBytes = java_env->GetMethodID(env->FindClass("java/lang/String"), "getBytes", "()[B");
    char * cereprocLicpath = convertString(java_env, lic);
    char * cereprocVocpath = convertString(java_env, voc);
    char * cereprocAudpath = convertString(java_env, aud);

	printf("CEREPROC---Licence file: %s. Voice path: %s.\n",cereprocLicpath,cereprocVocpath);
	fflush(stdout);

	CereProc * cere = new CereProc();
	cere->initCereProc(cereprocLicpath, cereprocVocpath, cereprocAudpath);

	//envoi du pointeur de cereproc vers java
    long lp = (long)cere;
	return lp;
}

JNIEXPORT jobjectArray JNICALL Java_vib_auxiliary_tts_cereproc_nativetts_CereProcTTS_speak (JNIEnv * env, jobject obj, jstring text, jstring audioName, jlong ptr) {
    java_env = env;
    java_env->GetJavaVM(&jvm);
    char * textSAPI = convertString(java_env, text);
    char * audName = convertString(java_env, audioName);
	//recuperation du CereProc
	CereProc * cere = (CereProc *)ptr;

	//vec est le vecteur des annotations
	std::vector<jstring> vec = cere->speak(env, textSAPI, audName);
	int size = vec.size();

	//conversion des annotations pour envoi sur JNI
	jobjectArray jobjarray;
	jobjarray = (jobjectArray) env->NewObjectArray(size,env->FindClass("java/lang/String"), env->NewStringUTF(""));

	for(int i=0;i<size;i++){
		env->SetObjectArrayElement(jobjarray,i,vec.at(i));
	}
	return jobjarray;
}
