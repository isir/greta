from __future__ import division

import os
import re
import subprocess

def getFileFormat(input_file, trace):
    """
    Function to get the file extension
    input: file path
    output: audio file extension
    """
    file_format 	= os.path.splitext(input_file)[1][1:]
    if(trace == True):
        print("getFileFormat -> " + file_format)
    return file_format

def getFileName(file_path, trace):
    """
    getFileName
    Function to get the name of a file given his path
    input: file path
    output: file name
    """
    # cut the filename according to /
    output_name 	= os.path.split(os.path.splitext(file_path)[0])[-1]
    if(trace == True):
        print("getFileName ->  " + output_name)
    # return the last field of the path corresponding to the filename
    return output_name 

def getNumChannels(audio_file, trace):
    """
    # Input:    audio file
    # Output:   channel number
    """
    command = ["supervp", "-E0", "-S",os.path.abspath(audio_file), "-v"]

    with open(os.devnull, "wb") as FNULL:
        process = subprocess.Popen(command, stdout = subprocess.PIPE,
                                   stderr = subprocess.PIPE, universal_newlines=True, shell = False)
        out, err = process.communicate()
    if process.returncode:
        raise RuntimeError('supervp command {0} failed with error code:: {1:d}\n{2}{3}'.format(" ".join(command),
                                                                                            process.returncode,
                                                                                            out, err));
    get_channels = mygrep("number of channels", ':', err)
    print(get_channels)

    tchannel = get_channels[0]
    tchannel = re.sub(" ", "", tchannel)

    print(tchannel)

    if tchannel == "stereo":
        ret = 2
    elif tchannel == "mono":
        ret = 1
    if trace:
        print("channels -> " + str(ret))

    return ret

def mygrep(field_to_extract, separator, input_text):
    """
    mygrep (subfunction only)

    Field to extract
     Input: field to extract + separator + input_file
     Output: expected field
    """
    #try :
    split_text 	= input_text.split("\n")
    #except TypeError:
    #    split_text 	= input_text.decode().split("\n")
        
    greps 	= []
    for line in split_text:
        if(field_to_extract in line):
            #Return the symbol placed after the separator
            greps.append(line.partition(separator)[2].rstrip())
    if greps == []:
        raise RuntimeError ("mygrep::error : No such field to extract")

    try:
        if(greps[1] != None):
            return greps
    except:
        return greps[0]

def mygrep_not(field_not_to_extract, separator, input_text):
    """
    #mygrep_not (subfunction only)
    #Extract all fields but the selected one
    # Input: field not to extract + input_file
    # Output: expected fields
    """
    split_text 	= input_text.split("\n")
    greps 	= []
    for line in split_text:
        if(field_not_to_extract not in line):
            #Give the field after the chosen symbol 
            greps.append(line.partition(separator)[2].rstrip())
    return greps

# getSamplingRate (subfunction only)
# Input : audio file 
# Output : rate sample
def getSamplingRate(audio_file, trace):
    command = ["supervp", "-E0", "-S",os.path.abspath(audio_file), "-v"]

    with open(os.devnull, "wb") as FNULL:
        process = subprocess.Popen(command, stdout = FNULL,
                                    stderr = subprocess.PIPE, universal_newlines=True, shell = False)
        out , err = process.communicate()
        
    if process.returncode : 
        raise RuntimeError('supervp command {0} failed with error code:: {1:d}\n{2}{3}'.format(" ".join(command),
                                                                                             process.returncode,
                                                                                             out, err));
    get_sampling 	= mygrep("sampling rate", ':', err)
    get_sampling 	= re.sub("[a-zA-Z]", "", get_sampling[0])
    get_sampling 	= re.sub(" ", "", get_sampling)
    return float(get_sampling)

# checkAudioFile	
# Function to check and convert the soundfile sound rate (SR) to wav format
# Input: file name + general conf path
# Output: wav_file name
def checkAudioFile(audio_file, audioNorm_file, audioNorm_file_SR, trace):
    sampling_rate = getSamplingRate(audio_file, trace)
    file_SR = sampling_rate * 1000
    audioNorm_file_SR_div_1000 = sampling_rate

    file_format = getFileFormat(audio_file, trace)
    channels    = getNumChannels(audio_file, trace)

    if file_format == "wav" and file_SR == audioNorm_file_SR and channels == 1:
        subprocess.call(["cp", audio_file, audioNorm_file], shell = False)
        return

    command = [ "supervp" ]

    if file_SR != audioNorm_file_SR:
        command = command + ["-H{0:d}".format(int(audioNorm_file_SR))]
    
    if channels != 1:
        command = command + ["-C1"]

    command = command + ["-S", audio_file, "-Osw", audioNorm_file]
    if trace :
        print(" ".join(command))
    process = subprocess.Popen(command, stdout=subprocess.PIPE,
                                stderr=subprocess.STDOUT, universal_newlines=True, shell=False)
    errout_and_output, unused_output = process.communicate()
    if process.returncode:        
        raise RuntimeError('supervp command: {0}\nfailed with error code:: {1:d}\n{2}'.format(" ".join(command),
                                                                                              process.returncode,
                                                                                              errout_and_output));
    if trace :
        print("generated", audioNorm_file)
