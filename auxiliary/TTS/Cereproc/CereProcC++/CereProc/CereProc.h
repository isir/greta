// CereProc.cpp : définit le point d'entrée pour l'application console.
//

#include "stdafx.h"
#include <stdio.h>
#include <string.h>
#include <cerevoice_eng.h>
#include <vector>
#include <jni.h>

class Cereproc
{
private:
	
	CPRCEN_engine * eng;
	CPRCEN_channel_handle chan;
	char * audpath;

public:
	Cereproc() {}
	
	void Cereproc::close();
	std::vector<jstring> Cereproc::speak(JNIEnv * env,char txt[], char audioName[]);

	void Cereproc::initCereproc(char*, char*, char*);
	
};
