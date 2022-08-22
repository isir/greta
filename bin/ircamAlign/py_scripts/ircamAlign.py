#! /usr/bin/env python3

from __future__ import print_function, absolute_import, division

import sys
import subprocess 
import os.path
import argparse

import ircamAlign_func
import IA_support

#Test Args and options

version = "unknown"
try :
    with open(os.path.join(os.path.dirname(__file__), "version.txt"), "r") as ff:
        vv=ff.readline()
        version = vv.strip()
except IOError :
    # try root dir
    with open(os.path.join(os.path.dirname(os.path.dirname(__file__)), "version.txt"), "r") as ff:
        vv=ff.readline()
        version = vv.strip()

description ="""
{0}::Version {1} 
""".format(os.path.splitext(os.path.basename(__file__))[0], version)

epilog = """===================================================================
Copyright 2013-2021 IRCAM 
-------------------------------------------------------------------
based on HTK 3.4.1 (see: http://htk.eng.cam.ac.uk) and LIA_PHON
===================================================================
"""

parser = argparse.ArgumentParser(prog=os.path.splitext(os.path.basename(__file__))[0],
                                        epilog=epilog,
                                        description = description,
                                        formatter_class=argparse.RawTextHelpFormatter)
parser.add_argument("wavfile", nargs="?", default=None, help="wavfile to be used for alignemnt")
parser.add_argument("-t", "--txt_file", default=None, type=str, help="text file containg the text corresponding to the audiofile to be aligned, format has to be latin1!")
parser.add_argument("-l","--lia_phonetisation_flag",  action="store_true", help=" use lia_phonetisation with variants (lia_text2phon_lattice instead of lia_text2phon)" )
parser.add_argument("-w","--wavesurfer_flag",  action="store_true", help="display result in wave surfer" )
parser.add_argument("-lp","--lia_permissive_phonetisation",
                    action="store_true",
                    help="Does not throw an error if any characters in the input text cannot be encoded in latin1 for communication with liaphon. "
                         "If set these characters will simply be ignoredill be ignored." )
parser.add_argument("--version", action="store_true", help="display ircamAlign version and exit")
parser.add_argument("-r","--do_reco",  action="store_true", help="do syllable, word and phrase recognition (Def: off)" )
parser.add_argument("-a","--as_flag",  action="store_true", help="display result in audiosculpt (Mac OS only)" )
parser.add_argument("-o","--output", type=str, default=os.getcwd(), help="output directory (def: %(default)s)" )
parser.add_argument("-v","--verbose", action="store_true", help="verbose processing (def: %(default)s)" )
#parser.add_argument("-X","--is_latin1", action="store_true", help="by default ircamAlign expects utf-8 encoding of the input file this flag request decoding from latin1 (Def: %(default)s)" )
install_dir_def = os.path.dirname(os.path.dirname(os.path.realpath(__file__)))
parser.add_argument("-i","--install_dir", type=str, default=install_dir_def, help="install directory this flag is for debugging only (def: {0})".format(install_dir_def))

# first version of args does not
args = parser.parse_args()

if args.version :
    print("{0}::Version {1}".format(os.path.basename(__file__), version))
    sys.exit(0)

if args.wavfile is None:
    parser.error("too few arguments")

#Test if there is a text to analyse
txt_file = args.txt_file

#No (more) options
audio_file = args.wavfile

#Set the output and flags
output_dir = args.output
#confidence_flag 		= False
lia_phonetisation_flag 	= args.lia_phonetisation_flag
lia_strict_phonetisation 	= not args.lia_permissive_phonetisation
wavesurfer_flag 		= args.wavesurfer_flag

#accessor to the type of separator
sep 			= ircamAlign_func.separator()

#INSTALLER NEED TO BE IN THE PARENT FOLDER
INSTALL_DIR 		= args.install_dir
LIA_PHON_REP 		= os.path.join(INSTALL_DIR, "liaphon_build") + sep
HTK_BIN_DIR 		= os.path.join(INSTALL_DIR, "htk_build", "bin") + sep
sv56                = os.path.join(INSTALL_DIR, "sv56_build", "sv56")
os.environ["INSTALL_DIR"] 	= INSTALL_DIR
os.environ["HTK_BIN_DIR"] 	= HTK_BIN_DIR

if "SFDIR" in os.environ :
    del os.environ["SFDIR"]

model_dir 		= os.path.join(INSTALL_DIR, "models") + sep
modelMono_file 		= model_dir + "models"
modelTri_file 		= model_dir + "modelsTri4.mdl"
modelTriS_file 		= model_dir + "modelsTri4.mdl"
phon_file 		= model_dir + "phongram.list"
mono_file 		= model_dir + "monophones.list"
phonSolo_file 		= model_dir + "phongram_solo.list"
monoSolo_file 		= model_dir + "monophones_solo.list"
tri_file 		= model_dir + "triphones.list"
genConf_file 		= model_dir + "General.conf"
HCopyConf_file 		= model_dir + "HCopy.conf"
HViteConf_file 		= model_dir + "HVite.conf"
HCompVConf_file 	= model_dir + "HCompV.conf"
monoNumStates_file 	= model_dir + "monoNumStates.list"
ge_file 		= model_dir + "gH"

CM_threshold 		= 0.5
norm_tag 		= 1
trace 			= args.verbose
lia_tag 		= 1		 #default 1
short_pauses_tag= 3		 #default 3
br_tag 			= True	 #default True
words_tag 		= 0
binary_tag      = 0

genConf_tab     = ircamAlign_func.readFile(genConf_file,trace)
#level in dB
audio_level     = -23
audio_SR        = ircamAlign_func.getInfoGenConf("FE",
                                                 genConf_tab, trace)
target_rate 	= int(float(ircamAlign_func.getInfoGenConf("TARGETRATE",
                                                           genConf_tab, trace)))
audioName_file 		= IA_support.getFileName(audio_file, trace)
audio_format 		= IA_support.getFileFormat(audio_file, trace)
# Get name root
audioName_root 		= audioName_file

# File extension
mfcc_ext 		= "mfcc"
audioNorm_ext 		= "wav"
syn_ext 		= "syn"
synflat_ext 		= "synflat"
netflat_ext 		= "netflat"
labPhon_ext 		= "labPhon"
labPhonLIA_ext 		= "labPhonLIA"
labPhonXSAMPA_ext 	= "labPhonXSAMPA"
labSylLIA_ext 		= "labSylLIA"
labSylXSAMPA_ext 	= "labSylXSAMPA"
labPhraseLIA_ext 	= "labPhraseLIA"
labPhraseXSAMPA_ext 	= "labPhraseXSAMPA"
labSenonLIA_ext 	= "labSenonLIA"
labSemiphonLIA_ext 	= "labSemiphonLIA"
labSemiphonXSAMPA_ext 	= "labSemiphonXSAMPA"
labFrameCmLIA_ext 	= "labFrameCmLIA"
labFrameCmXSAMPA_ext 	= "labFrameCmXSAMPA"
labPhonCmLIA_ext 	= "labPhonCmLIA"
labPhonCmXSAMPA_ext 	= "labPhonCmXSAMPA"
labWordCmLIA_ext 	= "labWordCmLIA"
labWordCmXSAMPA_ext 	= "labWordCmXSAMPA"
labSylType_ext 		= "labSylType"
labPhrase_ext 		= "labPhrase"
labHTS_ext 		= "labHTS"

# Create directory names
WORK_DIR 		= output_dir + sep + audioName_root + "_ircamAlign"
# Deleted creation of one dedicated sub-folder per file type:
# assign working directory to each type
mfcc_dir 		= WORK_DIR
audioNorm_dir 		= WORK_DIR
syn_dir 		= WORK_DIR
synflat_dir 		= WORK_DIR
netflat_dir 		= WORK_DIR
labPhon_dir 		= WORK_DIR
labPhonLIA_dir 		= WORK_DIR
labPhonXSAMPA_dir 	= WORK_DIR
labSenonLIA_dir 	= WORK_DIR
labSemiphonLIA_dir 	= WORK_DIR
labSemiphonXSAMPA_dir 	= WORK_DIR
labSylLIA_dir 		= WORK_DIR
labSylXSAMPA_dir 	= WORK_DIR
labSylType_dir 		= WORK_DIR
labPhraseLIA_dir 	= WORK_DIR
labPhraseXSAMPA_dir 	= WORK_DIR
labPhrase_dir 		= WORK_DIR
labFrameCmLIA_dir 	= WORK_DIR
labFrameCmXSAMPA_dir 	= WORK_DIR
labPhonCmLIA_dir 	= WORK_DIR
labPhonCmXSAMPA_dir 	= WORK_DIR
labWordCmLIA_dir 	= WORK_DIR
labWordCmXSAMPA_dir 	= WORK_DIR
labHTS_dir 		= WORK_DIR

try:
    subprocess.check_output(["supervp"], stderr=subprocess.STDOUT)
except Exception as ex:
    raise RuntimeError("can not run supervp command, please make sure you have supervp in your PATH Mesg: {0}".format(str(ex)))

# Create working directory
subprocess.call("mkdir -p "+ WORK_DIR, shell = True)
print("results are put in " + WORK_DIR)

# Build file name
mfcc_file 		= mfcc_dir + sep + audioName_root + "." + mfcc_ext
audioNorm_file 		= audioNorm_dir + sep + audioName_root + "." 	\
			+ audioNorm_ext
audio_sr_file 		= audioNorm_dir + sep + audioName_root + "_sr." \
			+ audioNorm_ext
syn_file 		= syn_dir + sep + audioName_root + "." + syn_ext
synflat_file 		= synflat_dir + sep + audioName_root + "." 	\
			+ synflat_ext
netflat_file 		= netflat_dir + sep + audioName_root + "." 	\
			+ netflat_ext
labPhon_file 		= labPhon_dir + sep + audioName_root + "."	\
			+ labPhon_ext
labPhonLIA_file 	= labPhonLIA_dir + sep + audioName_root + "."	\
			+ labPhonLIA_ext
labPhonXSAMPA_file 	= labPhonXSAMPA_dir + sep + audioName_root + "."\
			+ labPhonXSAMPA_ext
labSylLIA_file 		= labSylLIA_dir + sep + audioName_root + "." 	\
			+ labSylLIA_ext
labSylXSAMPA_file 	= labSylXSAMPA_dir + sep + audioName_root + "."	\
			+ labSylXSAMPA_ext
labSylType_file 	= labSylType_dir + sep + audioName_root + "." 	\
			+ labSylType_ext
labPhraseLIA_file 	= labPhraseLIA_dir + sep + audioName_root + "." \
			+ labPhraseLIA_ext
labPhraseXSAMPA_file 	= labPhraseXSAMPA_dir + sep + audioName_root + "."\
			+ labPhraseXSAMPA_ext
labPhrase_file 		= labPhrase_dir + sep + audioName_root + "."	\
			+ labPhrase_ext
labSenonLIA_file 	= labSenonLIA_dir + sep + audioName_root + "." 	\
			+ labSenonLIA_ext
labSemiphonLIA_file 	= labSemiphonLIA_dir + sep + audioName_root + "."\
			+ labSemiphonLIA_ext
labSemiphonXSAMPA_file 	= labSemiphonXSAMPA_dir + sep + audioName_root + "."\
			+ labSemiphonXSAMPA_ext
labFrameCmLIA_file 	= labFrameCmLIA_dir + sep + audioName_root + "."\
			+ labFrameCmLIA_ext
labFrameCmXSAMPA_file 	= labFrameCmXSAMPA_dir + sep + audioName_root + "."\
			+ labFrameCmXSAMPA_ext
labPhonCmLIA_file 	= labPhonCmLIA_dir + sep + audioName_root + "." \
			+ labPhonCmLIA_ext
labPhonCmXSAMPA_file 	= labPhonCmXSAMPA_dir + sep + audioName_root + "."\
			+ labPhonCmXSAMPA_ext
labWordCmLIA_file 	= labWordCmLIA_dir + sep + audioName_root + "." \
			+ labWordCmLIA_ext
labWordCmXSAMPA_file 	= labWordCmXSAMPA_dir + sep + audioName_root + "."\
			+ labWordCmXSAMPA_ext
labHTS_file 		= labHTS_dir + sep + audioName_root + "." + labHTS_ext

lia_path 		= WORK_DIR + sep + audioName_root + ".lia"

if lia_phonetisation_flag :
    print("  *Use lia phonetisation..")
    lia_tag          = 0 #default 1
    short_pauses_tag = 0 #default 3
    br_tag           = 0 #default 1


# compute MFCC
if(trace == True):
    print("***Compute Mfcc...\n")
ircamAlign_func.computeMFCC(audio_file, audio_sr_file,audioNorm_file,
                          mfcc_file, HCopyConf_file, audio_SR,
                          audio_level, norm_tag, sv56, trace)

if (txt_file != ""):
  gram_tag 		= 0
  waveSurferConfig	= model_dir + "segmentationXSAMPA.conf"
  if(trace == True):
    print("***Build graph from txt_file")

  # File extension
  txt_ext 		= "txt"
  net_ext 		= "net"
  reg_ext 		= "reg"
  labWord_ext 		= "labWord"
  labWordLIA_ext 	= "labWordLIA"
  labWordXSAMPA_ext 	= "labWordXSAMPA"
  labPosLIA_ext 	= "labPosLIA"

  # Create directories names
  #(avoid dedicated folder per type: assign working dir)
  net_dir 		= WORK_DIR
  reg_dir 		= WORK_DIR
  labWord_dir 		= WORK_DIR
  labWordLIA_dir 	= WORK_DIR
  labWordXSAMPA_dir 	= WORK_DIR
  labPosLIA_dir 	= WORK_DIR

# Build file name
  net_file 		= net_dir + sep + audioName_root + "." + net_ext
  reg_file 		= reg_dir + sep + audioName_root + "." + reg_ext
  labWord_file 		= labWord_dir + sep + audioName_root + "."	+ labWord_ext
  labWordLIA_file 	= labWordLIA_dir + sep + audioName_root + "." + labWordLIA_ext
  labWordXSAMPA_file 	= labWordXSAMPA_dir + sep + audioName_root + "." + labWordXSAMPA_ext
  labPosLIA_file 	= labPosLIA_dir + sep + audioName_root + "." + labPosLIA_ext
  regexp_tab = ircamAlign_func.phonetise(txt_file, syn_file,
                                         net_file, lia_path, lia_tag,
                                         short_pauses_tag, br_tag,
                                         words_tag, binary_tag,
                                         lia_strict_phonetisation=lia_strict_phonetisation,
                                         lia_phon_dir=LIA_PHON_REP,
                                         trace=trace)
  try:
    subprocess.call("rm  " + reg_file, shell = True, stderr = subprocess.PIPE)
  except:
    pass
  ircamAlign_func.many_writeFile(reg_file, regexp_tab, trace)
else:
  if(trace == True):
    print("***Use Bi-gram Language Model")
  waveSurferConfig 	= model_dir + "segmentationWithoutWordCM.conf"
  gram_tag 		= 1
  net_file 		= model_dir + "dap.net"

if(trace == True):
  print("***Align...\n")

ircamAlign_func.alignSoloTriphones(mfcc_file, net_file, netflat_file,
                                   labPhonLIA_file, labSenonLIA_file, syn_file,
                                   synflat_file,HViteConf_file, modelMono_file,
                                   modelTri_file, modelTriS_file, phon_file, mono_file,
                                   tri_file, binary_tag, gram_tag,lia_phonetisation_flag,
                                   target_rate, trace)

ircamAlign_func.remove_if_exists(labSemiphonLIA_file)
ircamAlign_func.makeSemiphon(labPhonLIA_file, labSemiphonLIA_file,
                             target_rate, trace)


if(txt_file != "" and args.do_reco):
  ircamAlign_func.recoSyllables(labPhonLIA_file, labSylLIA_file,
                                labPhon_file, labSylType_file, trace)
  ircamAlign_func.recoPhrase(labPhonLIA_file, labPhraseLIA_file, trace)
  ircamAlign_func.recoWords(labPhonLIA_file, labWord_file,
                            labPhrase_file, labWordLIA_file,
                            labPosLIA_file, regexp_tab, words_tag, trace)

ircamAlign_func.convertFormat(labPhonLIA_file, labPhonXSAMPA_file, trace)
ircamAlign_func.convertTime(labPhonXSAMPA_file, labPhonXSAMPA_file, trace)
ircamAlign_func.convertFormat(labSemiphonLIA_file,
			      labSemiphonXSAMPA_file, trace)
ircamAlign_func.convertTime(labSemiphonXSAMPA_file,
			    labSemiphonXSAMPA_file, trace)

if(txt_file != ""  and args.do_reco):
    ircamAlign_func.convertTime(labPhon_file, labPhon_file, trace)
    ircamAlign_func.convertFormat(labWordLIA_file, labWordXSAMPA_file, trace)
    ircamAlign_func.convertTime(labWordXSAMPA_file, labWordXSAMPA_file, trace)
    ircamAlign_func.convertTime(labWord_file, labWord_file, trace)
    ircamAlign_func.convertTime(labPhrase_file, labPhrase_file, trace)
    ircamAlign_func.convertFormat(labSylLIA_file, labSylXSAMPA_file, trace)
    ircamAlign_func.convertTime(labSylXSAMPA_file, labSylXSAMPA_file, trace)
    ircamAlign_func.convertTime(labSylType_file, labSylType_file, trace)
    ircamAlign_func.convertTime(labPosLIA_file, labPosLIA_file, trace)
    ircamAlign_func.convertFormat(labPhraseLIA_file, labPhraseXSAMPA_file, trace)
    ircamAlign_func.convertTime(labPhraseXSAMPA_file, labPhraseXSAMPA_file, trace)

#labPhonXSAMPA used by default
if(wavesurfer_flag == True):
  out_file 	= labPhonXSAMPA_dir + sep + audioName_root + ".lab"
  subprocess.call("cp " + labPhonXSAMPA_file + " " + out_file, shell = True)
  subprocess.call("wavesurfer -sync" + waveSurferConfig + " " + audioNorm_file + " "
       + out_file, shell = True)

#labPhonXSAMPA used by default
if(args.as_flag == True):
  out_file 	= labPhonXSAMPA_dir + sep + audioName_root + ".lab"
  subprocess.call("cp " + labPhonXSAMPA_file + " " + out_file, shell = True)
  subprocess.call("open" + " " + audioNorm_file + " " + out_file, shell = True)
