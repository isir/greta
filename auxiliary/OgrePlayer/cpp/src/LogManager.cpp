#include "LogManager.h"
#include <OgreLogManager.h>

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_LogManager_set_1LL_1BOREME(JNIEnv *, jclass)
{
    Ogre::LogManager::getSingleton().setLogDetail(Ogre::LoggingLevel::LL_BOREME);
}

JNIEXPORT void JNICALL Java_vib_auxiliary_player_ogre_natives_LogManager_set_1LL_1LOW(JNIEnv *, jclass)
{
    Ogre::LogManager::getSingleton().setLogDetail(Ogre::LoggingLevel::LL_LOW);
}
