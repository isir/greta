#include <jni.h>
#include <string>
/* Header for class vib_auxiliary_player_ogre_natives__Object_ */

#ifndef _Included_vib_auxiliary_player_ogre_natives__Object_
#define _Included_vib_auxiliary_player_ogre_natives__Object_
#ifdef __cplusplus
extern "C" {
#endif

char* convertJStringToChar
  (JNIEnv *, jstring);

std::string& convertJStringToOgreString
  (JNIEnv *, jstring, std::string &);

jstring convertOgreStringToJString
  (JNIEnv *, std::string);

jstring convertCharToJString
  (JNIEnv *, char *);

#ifdef __cplusplus
}
#endif
#endif
