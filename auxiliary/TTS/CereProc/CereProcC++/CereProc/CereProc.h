// CereProc.cpp : définit le point d'entrée pour l'application console.
//

#include "stdafx.h"
#include <stdio.h>
#include <string.h>
#include <cerevoice_eng.h>
#include <vector>
#include <jni.h>

class CereProc
{
private:
	
	CPRCEN_engine * eng;
	CPRCEN_channel_handle chan;
	char * audpath;

public:
	CereProc() {}
	
	void CereProc::close();
	std::vector<jstring> CereProc::speak(JNIEnv * env,char txt[], char audioName[]);

	void CereProc::initCereProc(char*, char*, char*);
	
};
