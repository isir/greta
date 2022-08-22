from __future__ import division

import os
import re
import subprocess

import IA_support 

def audioSamplingLevelNorm(audio_in, audio_out, level, sv56, trace):
    """
    Normalized audio level
    # Input : audio_sr file
    # Output : normalised audio file 
    """
    #Conversion KHz to Hz
    sample_rate = IA_support.getSamplingRate(audio_in, trace) * 1000
    raw_in      = os.path.abspath(os.path.join(os.path.dirname(audio_out), "in.raw"))
    raw_out 	= os.path.abspath(os.path.join(os.path.dirname(audio_out), "out.raw"))
    input_file 			= "-S"
    wav_16_bit_int_output_mode 	= "-Osw"
    raw_32_bit_float_output_mode 	= "-Osrf"
    sampling_frequency_to 	= "-sf"
    desired_level_for_the_output 	= "-lev"
    #four byte
    byte_float_format 		= "-float"
    command = ["supervp", "-C1", input_file, os.path.abspath(audio_in), raw_32_bit_float_output_mode, raw_in]
    process = subprocess.Popen(command, universal_newlines=True, shell = False)
    out, err = process.communicate()
    if process.returncode:
        raise RuntimeError('command <{0}> failed with error code:: {1:d}\n{2}{3}'.format(" ".join(command),
                                                                                         process.returncode,
                                                                                         out, err));
        
    # Equalize with speech voltmeter
    command = [sv56, sampling_frequency_to, str(sample_rate),
               desired_level_for_the_output, str(level), byte_float_format, raw_in, raw_out]
    process = subprocess.Popen(command, stdout = subprocess.PIPE,
                                   stderr = subprocess.STDOUT, universal_newlines=True, shell = False)
    out, err = process.communicate()
    if process.returncode:
        raise RuntimeError('command <{0}> failed with error code:: {1:d}\n{2}{3}'.format(" ".join(command),
                                                                                         process.returncode,
                                                                                         out, err))

    if (re.search('SV-I-MAXLEVDB', out) != None):
        if(trace == True):
            print("SATURATION:" + audio_in)
            print(out)
        get_level 	= IA_support.mygrep("Max norm WITHOUT saturation: ", ". ", out)
        get_level 	= re.sub("[a-zA-Z]", "", get_level)
        #Get the level value minus one ensure dB max norm respect
        get_level 	= float(re.sub("[\[\]]", "", get_level)) - 1
        
        command =[sv56, sampling_frequency_to,str(sample_rate), desired_level_for_the_output, str(get_level), byte_float_format,
                  raw_in, raw_out]
        process = subprocess.Popen(command, stdout = subprocess.PIPE,
                                       stderr = subprocess.STDOUT, universal_newlines=True, shell = False)
        out, err = process.communicate()
        if process.returncode:
            raise RuntimeError('command <{0}> failed with error code:: {1:d}\n{2}{3}'.format(" ".join(command),
                                                                                             process.returncode,
                                                                                             out, err));
        if (re.search('SV-I-MAXLEVDB', out) != None):
            os.remove(raw_out)
            raise RuntimeError("sv56: audio normalization failed due tio saturation\ncommand={1}\nMSG:{2}".format(" ".join(command),
                                                                                                              out, err))

    # Convert back to wav
    command 	= ["supervp", "-C1", "-R"+str(sample_rate), input_file, raw_out, wav_16_bit_int_output_mode, audio_out]
    if trace:
        print(" ".join(command))
    process = subprocess.Popen(command, universal_newlines =True,  shell = False)
    out, err = process.communicate()
    if process.returncode:
        raise RuntimeError('command <{0}> failed with error code:: {1:d}\n{2}{3}'.format(" ".join(command),
                                                                                         process.returncode,
                                                                                         out, err));
  
    subprocess.call(["rm", raw_in], shell = False)
    subprocess.call(["rm", raw_out], shell = False)
