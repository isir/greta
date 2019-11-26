// CereProc.cpp : définit le point d'entrée pour l'application console.
//

#include "stdafx.h"
#include <stdio.h>
#include <string.h>
#include <cerevoice_eng.h>
#include <fstream>
#include <vector>
#include <jni.h>
#include <sstream>

#include "CereProc.h"
using namespace std;

//simple function to check if audio file exists
bool fexists(const char *filename)
{
  std::ifstream ifile(filename);
  return ifile.good();
}

void CereProc::close() {
	if(fexists(audpath))
		remove(audpath);
	CPRCEN_engine_clear_callback(eng,chan);
	CPRCEN_engine_delete(eng);
}

vector<jstring> CereProc::speak(JNIEnv * env,char txt[],char audioName[]) {
	CPRC_abuf * abuf;
	const CPRC_abuf_trans * trans;
	float start, end;
	const char* name;
	vector<jstring> annotations;
	stringstream ss (stringstream::in | stringstream::out);
	
	printf("audiopath ");
	audpath=audioName;
	printf(audpath);
	CPRCEN_engine_channel_to_file(eng, chan, audpath, CPRCEN_RIFF);



	printf(txt);
	//remove the temp audio file and clear the audio buffer and annotations
	if(fexists(audpath))
		remove(audpath);
	CPRCEN_engine_clear_callback(eng,chan);
	annotations.clear();

	abuf = CPRCEN_engine_channel_speak(eng, chan, txt, strlen(txt), 1);
	if(abuf)
	{
		//loop through the annotations of the audio buffer
		for(int i = 0; i < CPRC_abuf_trans_sz(abuf); i++) {
			trans = CPRC_abuf_get_trans(abuf, i);
			start = CPRC_abuf_trans_start(trans); /* Start time in seconds */
			end = CPRC_abuf_trans_end(trans); /* End time in seconds */
			name = CPRC_abuf_trans_name(trans); /* Label, type dependent */
			if (CPRC_abuf_trans_type(trans) == CPRC_ABUF_TRANS_PHONE) {
					//a phoneme annotation is sent like this : start time, "/", end time, "/", phoneme type
					ss.str("");
					ss << start << "/" << end <<"/"<<name;
					annotations.push_back( env->NewStringUTF(ss.str().c_str()));
			}
			//keeping this in case we need the other annotations in the future
			/* else if (CPRC_abuf_trans_type(trans) == CPRC_ABUF_TRANS_WORD) {
				printf("INFO: word: %.3f %.3f %s\n", start, end, name);
					outputFile << start << " " << end << " "  << name <<std::endl;
			} */ else if (CPRC_abuf_trans_type(trans) == CPRC_ABUF_TRANS_MARK) {
					ss.str("");
					ss << start << "/" << end <<"/"<<"tmarker"<<name;
					annotations.push_back( env->NewStringUTF(ss.str().c_str()));

				//printf("INFO: marker: %.3f %.3f %s\n", start, end, name);
					//outputFile << start << " " << end << " "  << name <<std::endl;
			}else if (CPRC_abuf_trans_type(trans) == CPRC_ABUF_TRANS_ERROR) {
				printf("ERROR: could not retrieve transcription at '%d'", i);
			}
		}
		return annotations;
	} 
	else {
		fprintf(stderr, "ERROR: audio buffer empty\n");
		return annotations;
	}
}

void CereProc::initCereProc(char * licPath, char * vocPath, char* audiopath) {
	audpath = audiopath;
	if(!fexists(licPath)){
		fprintf(stderr, "ERROR: licence file doesn't exist '%s'\n", licPath);
	}
	if(!fexists(vocPath)){
		fprintf(stderr, "ERROR: voice file doesn't exis '%s'\n", vocPath);
	}

	eng = CPRCEN_engine_load(licPath, vocPath);   
	if (!eng) {
		fprintf(stderr, "ERROR: unable to load cereproc engine\n");
	}

	chan = CPRCEN_engine_open_default_channel(eng);

	CPRCEN_engine_channel_to_file(eng, chan, audiopath, CPRCEN_RIFF);
}
