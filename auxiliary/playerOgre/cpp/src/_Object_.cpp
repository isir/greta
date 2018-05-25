#include "_Object_.h"
#include <stdlib.h>

#if defined (_WIN32)
#  include <Windows.h>
#endif

std::string& convertJStringToOgreString(JNIEnv *env, jstring jstr, std::string &outString)
{
#if defined (_WIN32)
    const wchar_t* c_str = (const wchar_t*)env->GetStringChars(jstr, 0);
    int requiredSize = WideCharToMultiByte(CP_ACP, 0, c_str, -1, 0, 0, NULL, NULL);
    char* dest = new char[requiredSize];
    WideCharToMultiByte(CP_ACP, 0, c_str, -1, dest, requiredSize, NULL, NULL);
    outString = dest;
    delete[] dest;
    env->ReleaseStringChars(jstr, (const jchar*)c_str);
    return outString;
#else
    const char* c_str = env->GetStringUTFChars(jstr, 0);
    outString = c_str;
    env->ReleaseStringUTFChars(jstr, c_str);
    return outString;
#endif
}

jstring convertOgreStringToJString(JNIEnv *env, std::string ostr)
{
    return convertCharToJString(env, (char*)ostr.c_str());
}

jstring convertCharToJString(JNIEnv *env, char *cstr)
{
    return env->NewStringUTF(cstr);
}
