#!/usr/bin/python3
# -*- coding: utf-8 -*-

from __future__ import print_function, absolute_import, division

from sys import exit
import os.path
from os import sep
import re
import subprocess 
from math import floor
import codecs
import IA_support
import audioSRLevelNorm as aslnorm
import sys

sys.stdout = open(sys.stdout.fileno(), mode='w', encoding='utf8', buffering=1)
sys.stderr = open(sys.stdout.fileno(), mode='w', encoding='utf8', buffering=1)

#Accesors to environment variables
def install_dir():
    return os.getenv('INSTALL_DIR')

def htk_bin_dir():
    return os.getenv('HTK_BIN_DIR')

def lia_phon_rep():
    return os.getenv('LIA_PHON_REP')

#separator : give the possibility to make a windows platform of the project
#accessor to the separator type
def separator():    
    return sep

def readFile_old(input_file, trace):
    open_file     = open(input_file, "r")
    get_text      = open_file.read()
    open_file.close()
    if(trace == True):
        print("readFile -> " + input_file)
    return get_text

def readFile(input_file, trace, detect_latin1=False):
    """
    read files encoding, force output to internal encoding
    """
    with open(input_file, "rb") as fi:
        B_get_text      = fi.read()

    # check and remove BOM (see https://stackoverflow.com/questions/13590749/reading-unicode-file-data-with-bom-chars-in-python)
    # BOM_UTF32_LE's start is equal to BOM_UTF16_LE so need to try the former first
    for enc, boms in \
            ('utf-8-sig', (codecs.BOM_UTF8,)), \
            ('utf-32', (codecs.BOM_UTF32_LE, codecs.BOM_UTF32_BE)), \
            ('utf-16', (codecs.BOM_UTF16_LE, codecs.BOM_UTF16_BE)) :
        for bom in boms:
            if B_get_text.startswith(bom):
                B_get_text = B_get_text[len(bom):]
                break

    # check latin1 encoding
    is_latin1 = False
    if detect_latin1:
        is_latin1 = True
        for cc in set(B_get_text):
            if not ((0 < cc <= 127)
                    or (cc in  [0xC0, 0xC1, 0xC2,
                                0xC4,
                                0xC7,
                                0xC8, 0xC9, 0xCA, 0xCB, 0xCE, 0xCF,
                                0xD2, 0xD3, 0xD4, 0xD6,
                                0xD9, 0xDA, 0xDB, 0xDC,
                                0xDF,
                                0xE0, 0xE1, 0xE2,
                                0xE4,
                                0xE7,
                                0xE8, 0xE9, 0xEA, 0xEB, 0xEE,  0xEF,
                                0xF2, 0xF3, 0xF4, 0xF6,
                                0xF9, 0xFA, 0xFB, 0xFC])):
                is_latin1 = False

        if trace :
            if is_latin1:
                print("detected latin1 encoding")
            else:
                print("no latin1 so assume utf8 encoding")

    if is_latin1:
        get_text = B_get_text.decode(encoding="latin1")
    else:
        get_text=B_get_text.decode()

    if(trace == True):
        print("readFile -> " + input_file)
    return get_text

def writeFile(input_file, content, trace):
    open_file     = open(input_file, "a", encoding="utf8")
    open_file.write(content)
    open_file.close()
    if(trace == True):
        print("writeFile -> " + input_file)

def many_writeFile(input_file, content, trace):
    for elem in content :
        writeFile(input_file, elem, False)
    if(trace == True):
        print("writeFile -> " + input_file)

def remove_if_exists(input_file):
    if os.path.exists(input_file):
            os.remove(input_file)


# getFieldGenConf (subfunction only)
# Function to get a field (a line) in genConf
# Input: general config path + field to extract + negation tag
# Output: expected field
def getFieldGenConf(field_to_extract, negation_tag,
                                        general_config_path, trace):
    if(negation_tag == 0):
        # get the necessary fields in general config and write them in HCopy.conf
        expected_grep       = IA_support.mygrep(field_to_extract, '=', general_config_path)
        if(trace == True):
            print("getFieldGenConf -> \"" + field_to_extract + "\" value")
    else: #if(negation_tag == 1):
        # get the necessary fields in general config and write them in HCopy.conf
        expected_grep       = IA_support.mygrep_not(field_to_extract, general_config_path)  
        if(trace == True):
            print("getFieldGenConf -> !\"" + field_to_extract + "\" value")
    return expected_grep
                                                
# getInfoGenConf
# Function to get an info in genConf
# Input: general config path + field to extract + negation tag
# Output: expected value field
def getInfoGenConf(field_to_extract, general_config_path, trace):
    # get the first field corresponding to Fe
    grep = getFieldGenConf(field_to_extract, 0, general_config_path, trace)
    if(trace == True):
        print("getInfoGenConf -> get \"" + field_to_extract + "\" value: " + grep)
    return grep




# computeMFCC
# Convert audiofile to wavFile if necessary according to the training corpus Fe
# Normalize using audioSamplingLevelNorm
# Input: name of the soundfile + generalConf path
# Output: name of the mfcc file 
def computeMFCC(audio_file, audio_sr_file, audioNorm_file, mfcc_file,
                HCopyConf_file, audio_SR, audio_level, norm_tag, sv56, trace):
    # check format and sampling rate (SR)
    IA_support.checkAudioFile(audio_file, audio_sr_file, audio_SR, trace)
    if(norm_tag == 1):
        if trace:
            print("-Normalise using aslnorm...")
        aslnorm.audioSamplingLevelNorm(audio_sr_file, audioNorm_file, audio_level, sv56, trace)
    if trace:
        print("-Run HCopy...")
    htk_path      = htk_bin_dir()
    if os.path.exists(mfcc_file):
        os.remove(mfcc_file)
    #Convert the files according to HCopyConf_file configuration
    command       = [htk_path + "HCopy", "-C", HCopyConf_file, audioNorm_file, mfcc_file]
    if(trace == True):
        print(" ".join(command))
    # compute Features via HCopy
    subprocess.call(command, shell = False)
    if(trace == True):
        print("Done.")



# filtre
# Function for text filtering
def filtre(sentence, trace):
    # remove "\n"
    sentence      = sentence.rstrip()
    # remove spaces
    sentence      = sentence.strip()
    if trace:
        print("filtre ->  initial:\t\"" + sentence + "\"")
    sentence      = sentence.replace("Qu", "qu")
    # "Est" at the beginning of the sentence
    sentence      = re.sub('^Est', 'est', sentence)
    sentence      = re.sub('^Faut', 'faut', sentence)
    sentence      = re.sub('^Toutes', 'toutes', sentence)
    sentence      = sentence.replace("Mr", "monsieur") 
    sentence      = sentence.replace("Mlle", "mademoiselle")
    sentence      = sentence.replace("Mme", "madame")
    sentence      = sentence.replace("M\.", "monsieur") 
    sentence      = sentence.replace("grand\'tante", "grand tante")
    # Accents -> Portability ?
    sentence      = sentence.replace("grand\'mère", "grand mère")
    if trace:
        print("cleanup sentence <{0}>for latin1".format(sentence))
    sentence = sentence.replace(u"\u2013", "-").replace(u"\u2018", "'").replace(u"\u2019", "'").replace(u'\u202f', "oe")
    sentence = sentence.replace(u"…", u"...").replace(u"\u0153", u"oe").replace(u"é", u"é").replace(u"è", u"è")
    sentence = sentence.replace(u"à", u"à").replace(u"ê", u"ê").replace(u"ù", u"ù").replace(u"’", u"'")
    sentence = sentence.replace(u"«", u"'").replace(u"»", u"'").replace(u' ', u' ')
    sentence = sentence.replace(u"œ", u"oe").replace(u"’", "'")

    # The ponctuation characters might be misinterpreted by liaphon
    if False:
        ## deleting ! at the end of a phrase leads to errors
        ## we better replace it by .
        ## Similarly for ; should better be repaced by ,
        ponctuation   = [';', ':', '!', '?', '"', '_', '(', ')', '{', '}', '[', ']']
        for items in ponctuation:
            sentence    = sentence.replace(items, '')
            sentence    = re.sub('[\.]+', '.', sentence)
            # filter '-'
            sentence    = re.sub('^[\-]', '', sentence)
            # sentence = sentence.replace(/( - |- | -)/ )
    else:
        sentence = sentence.replace(";", ",")
        sentence = sentence.replace(":", ".")
        sentence = sentence.replace("?", ".")
        sentence = sentence.replace("!", ".")

        ponctuation = ['"', '_', '(', ')', '{', '}', '[', ']']
        for items in ponctuation:
            sentence = sentence.replace(items, '')
            sentence = re.sub('[\.]+', '.', sentence)
            # filter '-'
            sentence = re.sub('^[\-]', '', sentence)
            # sentence = sentence.replace(/( - |- | -)/ )


    if(trace == True):
        print("filtre ->  filtered:\t\"" + sentence + "\"")
    return sentence

# text2phon
# Function to call lia_text2phon
# input : string corresponding to the text + option tag
# output : file tmp.lia
def text2phonOpen(sentence, lia_path, lia_tag,
                  lia_phon_dir,
                  lia_strict_phonetisation=False,
                  trace=False):
    # Phonetise the sentence using lia_text2phon
    # create handle for lia_text2phon_variante piping

    # encode txt into latin1
    try:
        if trace:
            print("check sentence <{0}> for latin1 compatibility".format(sentence))
        sentence.encode("latin1")
        msg = None
    except UnicodeEncodeError:
        msg = """{level}::the phrase <{sentence}> 
contains characters that cannot be encoded in latin1.{warn_opt}
        """

    if msg:
        if lia_strict_phonetisation:
            warn_opt = ""
            msg = msg.format(level="ERROR", sentence=sentence, warn_opt=warn_opt)
            raise RuntimeError(msg)
        else:
            warn_opt = "\nThese characters will be ignored and potentially lead to problems later!"
            print("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!", file=sys.stderr)
            print(msg.format(level="WARNING", sentence=sentence, warn_opt=warn_opt), file=sys.stderr)
            print("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!", file=sys.stderr)

    lia_phon_env = dict(os.environ)
    lia_phon_env.update(LIA_PHON_REP=lia_phon_dir)

    if(lia_tag == 0):
        lia_text2phon        =  os.path.join(lia_phon_dir,"script","lia_text2phon.sh")
        if(trace == True):
            print("text2phonOpen ->  phonetization using lia_text2phon...")
        command           = [lia_text2phon]
        lia_pipe          = subprocess.Popen(command, shell = False,
                                             encoding="latin1",
                                             errors="ignore",
                                             stdin = subprocess.PIPE, stdout = subprocess.PIPE,
                                             stderr=subprocess.PIPE,
                                             env = lia_phon_env,
                                             universal_newlines=True)
        lia_tab, err = lia_pipe.communicate(sentence)
        if lia_pipe.returncode :
            print("lia_text2phon failed with the following error\n{0}".format(err))
            raise RuntimeError('lia_text2phon {0} failed with error code:: {1:d}\n{2}'.format(" ".join(command),
                                                                                        lia_pipe.returncode,
                                                                                        err));

    # create handle for lia_text2phon_lattice piping
    elif(lia_tag == 1):
        if(trace == True):
            print("text2phonOpen ->  phonetization using lia_text2phon_lattice2...")
        lia_text2phon_lattice =  os.path.join(lia_phon_dir,"script","lia_text2phon_lattice.sh")
        command           = [lia_text2phon_lattice]
        lia_pipe = subprocess.Popen(command, shell = False, stdin = subprocess.PIPE,
                                    encoding="latin1",
                                    errors="ignore",
                                    stdout = subprocess.PIPE, stderr=subprocess.PIPE,
                                    env=lia_phon_env,
                                    universal_newlines=True)
        # write into the pipe
        lia_tab, err = lia_pipe.communicate(sentence)
        if lia_pipe.returncode :
            print("lia_text2phon_lattice failed with the following error\n{0}".format(err))
            raise RuntimeError('lia_text2phon_lattice {0} failed with error code:: {1:d}\n{2}'.format(" ".join(command),
                                                                                        lia_pipe.returncode,
                                                                                        err));
    # non implemented option
    else:
        #if(lia_tag > 1)
        print("non implemented option for lia_tag = " + lia_tag )
        exit(0)
    if(trace == True):
        print("Done.")
        print(' '.join(command) +  sentence)
    lia_tab               = lia_tab.split('\n')
    lia_tab.pop()                         # pop the last []
    if(trace == True):
        print("text2phonOpen ->  add optionnal schwa between words...")
    lia_out               = []
    lia_out.append("<s> ## [ZTRM->EXCEPTION]\n")
    lia_tmp=["", "", ""]
    last_phon = ""

    # reading the pipe
    for line in lia_tab:
        if False:
            line                = line.rstrip()
            # Reduce to one space
            line                = line.replace("  ", " ")
            liasplit_tab        = line.split(' ')
            for i in range(0, liasplit_tab.count('')):
                liasplit_tab.remove('')
            word = liasplit_tab[1]
        else:
            liasplit_tab        = line.split()

        if len(liasplit_tab) == 2:
            # fix apparent liaphon bug. The phrase
            # <le TON n'est pas tous.>
            # does not produce 3 elements for the description of the letter  <n'>
            # It produces
            # n'  [ADVNE]
            # which in the following fails due to the missing phoneme.
            #
            # The problem seems triggered by the fact that the n' is following a word in capitals that is taken as an
            # acronym. By contrast
            # <le ton n'est pas tous.>
            # is  correctly translated into 3 elements
            # n' nn [ADVNE]
            # we fix this problem by means of inserting the <nn> when it is missing
            #
            # New error arrives for numbers followed by an letter of the same phoneme but followed by an apostroph
            # Il y a 10 s'est
            if liasplit_tab[0][1] == "'" :
                liasplit_tab.insert(1, last_phon)
        lia_tmp[0]=lia_tmp[0]+liasplit_tab[0]
        lia_tmp[1]=lia_tmp[1]+liasplit_tab[1]
        lia_tmp[2]=liasplit_tab[2]
        last_phon=liasplit_tab[1][-2:]

        if(liasplit_tab[0][-1] == '\''):
            flag=1
        else:
            flag=0
        if(liasplit_tab[0] == "euh"):
            word              = "|ee*eu*oe|"
        if((liasplit_tab[1] != "????") and (liasplit_tab[2] !=
                "[ZTRM->EXCEPTION]") and (liasplit_tab[0] != "-")):
            if (flag==0):
                lia_out.append(lia_tmp[0] + " " + lia_tmp[1] + " " + lia_tmp[2] + "\n")
        if(flag==0):
            lia_tmp=["", "", ""]
    lia_out.append("</s> ## [ZTRM->EXCEPTION]\n")
    if(trace == True):
        print("Done.")
    if lia_path:
        # erase temporary file tmp.lia
        if os.path.exists(lia_path):
            os.remove(lia_path)
        many_writeFile(lia_path, lia_out, trace)
        if(trace == True):
            print("writeFile -> " + lia_path)
    else:
        return lia_out
    
# replace_last (subfunction only)
def replace_last(string, old, new):
    li    = string.rsplit(old, 1)
    return new.join(li)


# buildGraph
# Function to build phonetic graph
# Input :  string corresponding to the text + options tag
# Output : string corresponding to the HTK phonetic graph
#
# lia_tag:  0: liaphon phonetisation on the whole sentence
#           1: liaphon phonetisation per word with different pronunciations
#
# short_pauses_tag:     0: without short pauses (only original ones)
#                       1: with short pauses
#                       2: with optional short pauses
#
# words_tag :   0: words can't be skipped or repeated
#               1: words can be skipped but not repeated
#               2: words can be skipped or repeated
def buildGraph(lia_path, short_pauses_tag, words_tag, with_br_tag, trace):
    # Variables declaration                                                       
    # Build the liaphon graph
    lia                   = readFile(lia_path, trace).rstrip()
    lia                   = lia.split('\n')
    if(with_br_tag == False):
        # initial characters of the graph
        liaphon_graph       = "((##{{"
    else:
        liaphon_graph       = "((##[[br]]{{"
    for line in lia:
        # delete last \n
        line                = line.rstrip() 
        words = [ww for ww in line.split(" ") if ww != ""]
        if((with_br_tag == True) and (words[1] == "ll" or words[1] == "ss"
                                                                    or words[1] == "kk"
                                                                    or words[1] == "mm" or words[1] == "jj")):
            # words concatenation
            liaphon_graph     = liaphon_graph + words[1]
        elif((words[1] != "##" and words[1] != "????")):
            # words concatenation
            liaphon_graph     = liaphon_graph + words[1]
            # add bracket around words (for skipping and repeat)
            liaphon_graph     = liaphon_graph + "}}{{"
        # for short pauses
        elif(( words[1] == "##" and (
                (with_br_tag == True  and words[2] == "[YPFAI]")
                or (with_br_tag == False  and words[2] == "[YPFAI]" or words[2] == "[YPFOR]" )))):
            # words concatenation
            liaphon_graph     = liaphon_graph + "sp"
            # add bracket around words (for skipping and repeat)
            liaphon_graph     = liaphon_graph + "}}{{"
    if(with_br_tag == False):
        # final characters of the graph
        liaphon_graph       = replace_last(liaphon_graph, "{{", "##))")
    else:
        # final characters of the graph
        liaphon_graph       = replace_last(liaphon_graph, "{{", "[[br]]##))")

    # Convert expressions of the form [__*__*]
    model                 = re.compile('\|(\w+\*)+\|')
    while(model.search(liaphon_graph) != None):
        liaphon_graph       = re.sub(r'(\|(\w+\*)+\|)', r'[[\1]]', liaphon_graph)
        liaphon_graph       = re.sub(r'(\|(\w+\*)+\w+\|)', r'[[\1]]', 
                                                                  liaphon_graph)
        liaphon_graph       = re.sub("\[\|", "[", liaphon_graph)
        liaphon_graph       = re.sub("\|\]", "]", liaphon_graph)
        liaphon_graph = re.sub("\*","||", liaphon_graph)
        liaphon_graph = re.sub("\|\|\]\]","]]", liaphon_graph)
        
    # Convert expressions of the form [__*__]
    model                 = re.compile("\|(\w\w\*)+\w\w\|")
    if(model.search(liaphon_graph) != None):
        liaphon_graph       = re.sub(r'(\|(\w+\*)+\w+\|)', r'~\1~', liaphon_graph)
        liaphon_graph = re.sub("\~\|","((", liaphon_graph)
        liaphon_graph = re.sub("\|\~","))", liaphon_graph)
        liaphon_graph = re.sub("\*","||", liaphon_graph)

    # Sp options:
    ## $short_pauses_tag: 0: without added short pauses (only original ones)
    #                     1: with short pauses
    #                     2: with optional short pauses
    print("short_pauses_tag", short_pauses_tag)
    print("with_br_tag", with_br_tag)
    print("liaphon_graph", liaphon_graph)
    if(short_pauses_tag == 0 and with_br_tag == True):    # with short pauses
        if(trace == True):
            print("buildGraph ->  with short pauses...")
        liaphon_graph       = re.sub('\{\{sp\}\}', 
                                                                  '}}[[sp||br||spbr||brsp]]{{',liaphon_graph)
    elif(short_pauses_tag == 1):                  # with short pauses
        if(trace == True):
            print("buildGraph ->  with short pauses...")
        liaphon_graph       = re.sub('\{\{sp\}\}', '', liaphon_graph)
        if(with_br_tag == False):
            liaphon_graph     = re.sub('\}\}\{\{', '}}sp{{', liaphon_graph)
        else:
            liaphon_graph     = re.sub('\}\}\{\{', '}}((sp||br)){{', liaphon_graph)
    elif(short_pauses_tag == 2):                  # with optional short pauses
        if(trace == True):
            print("buildGraph ->  with optional short pauses...")
        liaphon_graph       = re.sub('\{\{sp\}\}', '', liaphon_graph)
        liaphon_graph       = re.sub('\}\}\{\{', '}}[[sp]]{{', liaphon_graph)
    elif(short_pauses_tag == 3):                  # with optional short pauses
        if(trace == True):
            print("buildGraph ->  with optional short pauses...")
        liaphon_graph       = re.sub('\{\{sp\}\}', '', liaphon_graph)
        if(with_br_tag == False):
            liaphon_graph     = re.sub('\}\}\{\{', '}}[[sp]]{{', liaphon_graph)       
        else:
            liaphon_graph     = re.sub('\}\}\{\{', 
                                                                  '}}[[sp||br||spbr||brsp]]{{', liaphon_graph)
    else:
        if(trace == True):
            print("buildGraph ->  only original short pauses...")
        if(words_tag > 2):
            print("non implemented option for short_pauses_tag = " + words_tag )
            exit(0)
        if(trace == True):
            print("Done.")

    print("liaphon_graph", liaphon_graph)

    # Words options:
    # words_tag : 0: words can't be skipped or repeated
    #             1: words can be skipped but not repeated
    #             2: words can be skipped or repeated
    if(words_tag == 0):           # words can't be skipped or repeated
        if(trace == True):
            print("buildGraph ->  words can't be skipped or repeated...")
        liaphon_graph       = re.sub('\}\}', '', liaphon_graph)
        liaphon_graph       = re.sub('\{\{', '', liaphon_graph)

    elif(words_tag == 1):         # words can be skipped but not repeated
        if(trace == True):
            print("buildGraph ->  words can be skipped but not repeated...")
        liaphon_graph       = re.sub('\}\}', ']]', liaphon_graph)
        liaphon_graph       = re.sub('\{\{', '[[', liaphon_graph)

    else:
        if(words_tag > 2):
            print("non implemented option for short_pauses_tag = " + words_tag)
            exit(0)
        elif(trace == True):
            print("buildGraph ->  words can be skipped or repeated...")
            print("Done.")
    # Adding spaces between liaphon phonemes and graphs symbols
    model                 = re.compile("..")
    liaphon_graph_with_spaces     = ""
    if(model.search(liaphon_graph) != None):
        liaphon_list        = model.findall(liaphon_graph)
        for elem in liaphon_list:
            if(elem[1] == ']' and elem[0] != ']'):
                liaphon_graph_with_spaces = liaphon_graph_with_spaces           \
                                                                    + str(elem[0]) + " " + str(elem[1]) + " "
            elif(elem[0] == '['and elem[1] != '['):
                liaphon_graph_with_spaces = liaphon_graph_with_spaces           \
                                                                    + str(elem[0]) + " " + str(elem[1])
            else:
                liaphon_graph_with_spaces = liaphon_graph_with_spaces + str(elem) + " "
    # Convert liaphon characters to HTK ones
    HTK_graph             = liaphon_graph_with_spaces
    # Convert graphs symbols
    HTK_graph             = re.sub('\(\(', '(', HTK_graph)
    HTK_graph             = re.sub('\)\)', ')', HTK_graph)
    HTK_graph             = re.sub('\[\[', '[', HTK_graph)
    HTK_graph             = re.sub('\]\]', ']', HTK_graph)
    HTK_graph             = re.sub('\{\{', '{', HTK_graph)
    HTK_graph             = re.sub('\}\}', '}', HTK_graph)
    HTK_graph             = re.sub('\|\|', '|', HTK_graph)
    # Convert ah -> a (!!!!TO CHANGE!!!!)
    HTK_graph             = re.sub('ah', 'aa', HTK_graph)
    if(with_br_tag == True):
        HTK_graph           = re.sub('\[ ee \| eu \| oe \]',
                                                                  '( ee | eu | oe )', HTK_graph)
    HTK_graph             = re.sub(r'(\[ \w\w+ \| \w\w+ \])', r'(\1)', HTK_graph)
    HTK_graph             = re.sub('\(\[', '(', HTK_graph)
    HTK_graph             = re.sub('\]\)', ')', HTK_graph)
    if(trace == True):
        print("\n\nHTKGRAPH ICI!!!!!! " + HTK_graph + "\n\n")
    return HTK_graph

# buildRegexp
# create regexp word list with multiple pronunciation
# input sentence
# output : regexp word tab list with phonetisation and grammar
def buildRegexp(lia_path, trace):
    lia           = readFile(lia_path, trace).split('\n')
    lia.pop()
    if(trace == True):
        print("buildRegexp -> ")
    regexp        = []
    count         = 0
    iterator      = iter(lia)
    for line in iterator:
        # delete spaces repeat
        line        = re.sub("\s\s+", " ", line)
        # remove last newline
        line        = line.rstrip()
        # get the 2 field of lia_text2phon result
        elem        = line.split(" ")
        # convert ah to aa
        elem[1]     = elem[1].replace("ah", "aa")
        try:
            next_line = lia[count + 1]
            count     = count + 1
        except:
            if(line.startswith("</s>")):
                regexp.append(elem[1] + " " + elem[0] + " " + elem[2] + "\n")
            break
        # get the 2 field of lia_text2phon result
        next_elem   = next_line.split(" ")
        # convert ah to aa
        next_elem[1]= next_elem[1].replace("ah", "aa")
        # Write the regexp_file
        if(not (elem[1] == "????" 
                        or (elem[1] == "##" and elem[0] == ".") 
                        or elem[2] == "[YPFAI]" 
                        or elem[2] == "[YPFOR]" 
                        or elem[2] == "[YPFAI->EXCEPTION]" 
                        or elem[0] == "-")):
            word      = elem[1]
            elem[1]   = elem[0]
            
            # Convert expressions of the form [__*__*]
            model     = re.compile('\|(\w\w\*)+\|')
            in_while  = False
            while (model.search(word) != None):
                in_while        = True
                word    = re.sub("\*\|" , ")?" , word, 1)
                word    = re.sub("\|", "(?:", word, 1)
                word    = re.sub(r'(\(?:(\w\w\*)+\w\w\|)', r'\1)', word)
                word    = re.sub("\|\)", ")", word)
                word    = re.sub(r'(\|(\w\w\*)+\w\w\)\?)', r'(\1', word)
                word    = re.sub("\(\|", "(?:", word)
            # Convert expressions of the form [__*__]
            model     = re.compile("\|(\w\w\*)+\w\w\|")
            while (model.search(word) != None):
                word    = re.sub("\|", "(?:", word, 1)
                word    = re.sub("\|", ")", word, 1)
                word    = re.sub("\*", "|", word)
            model     = re.compile("\|(\w\w\|)+\w\w\|")
            while (model.search(word) != None):
                word    = re.sub(r'(\|\w\w\|\w\w\|)', r'~\1~', word)
                word    = re.sub("~\|", "(?:", word, 1)
                word    = re.sub("\|~", ")", word, 1)
            word      = re.sub("~", "", word)
            if(in_while == True):
                word    = re.sub("\*", "|", word)
                in_while = False
            elem[0]   = word
            if(next_elem[2] == "[YPFOR]"):
                # print(the regexp)
                regexp.append(elem[0] + " " + elem[1] + " " + elem[2] + " [YPFOR" + next_elem[0] + "]\n")
                next(iterator)
            elif(((next_elem[2] == "[YPFAI]") and (next_elem[0] == ":"))
                      or ((next_elem[2] == "[YPFAI]") and (next_elem[0] == ";"))):
                regexp.append(elem[0] + " " + elem[1] + " " + elem[2] + " [YPFOR" + next_elem[0] + "]\n")
                next(iterator)
            else:
                regexp.append(elem[0] + " " + elem[1] + " " + elem[2] + "\n")
    print("buildRegexp", regexp)
    if(trace == True):
        print("Done.")
    return regexp


def phonetise(txt_file, syn_file, net_file, lia_path,
              lia_tag, short_pauses_tag, br_tag, words_tag, binary_tag,
              lia_strict_phonetisation=True,
              lia_phon_dir=None,
              trace=False):
    """
    # phonetise
    # Compute HTK phonetic graph
    # Filtering the text
    # phonetization with multiple pronunciation
    # writing the syn file (HTK graph convention)
    # compute the net file using HParse
    # input : txt file, graphs options,
    # output : syn file, net file
    """
    htk_path              = htk_bin_dir()
    txt_tab               = readFile(txt_file, trace, detect_latin1=True).split('\n')
    if '' in txt_tab :
        txt_tab.remove('')
    txt_list              = ""
    for item in txt_tab:
        txt_list            = txt_list + item.rstrip()
    if trace:
        print("-Build HTK phonetic graph...")
    text2phonOpen(filtre(txt_list, trace), lia_path, lia_tag,
                  lia_strict_phonetisation=lia_strict_phonetisation,
                  lia_phon_dir=lia_phon_dir,
                  trace=trace)
    # phonetic graph construction
    phonetic_graph        = buildGraph(lia_path, short_pauses_tag, words_tag, br_tag, trace)
    if (phonetic_graph is None or
            phonetic_graph.startswith("( ## [ br ] [ br ]")):
        raise RuntimeError("buildGraph: error: unable to make the task")

    phonetic_graph        = re.sub("sp\s+##", "##", phonetic_graph)
    if trace:
        print("phonetise ->  " + phonetic_graph)
    regexp_tab            = buildRegexp(lia_path, trace)
    syn_tab               = phonetic_graph.split(" ")
    remove_if_exists(syn_file)

    print("phonetise syn_tab", syn_tab)
    print("phonetise regexp_tab", regexp_tab)
    tabMax = len(syn_tab)
    for count in range(0, tabMax - 1):
        writeFile(syn_file, syn_tab[count] + " ", False)
    writeFile(syn_file, syn_tab[tabMax - 1] + "\n", False)
    if(trace == True):
        print("writeFile -> " + syn_file)
    syn                   = readFile(syn_file, trace)
    if(trace == True):
        print("-Create the HTK net file using HParse with binary tag "
                    + str(binary_tag) + " and input file " + syn_file + " ...")
    if (binary_tag == 1):
        # using HParse to compute the phonetic graph in binary format
        command = [htk_path + "HParse","-b", syn_file, net_file]
    else:
        # using HParse to compute the phonetic graph
        command = [htk_path + "HParse", syn_file, net_file]

    ret = subprocess.call(command, shell = False)
    if ret:
            raise RuntimeError("HParse failed:\n{0}".format(" ".join(command)))
    if(trace == True):
        print("Done.")
    # erase temporary file tmp.lia
    subprocess.call(["rm", lia_path], shell = False)
    return regexp_tab


# alignSoloTriphones
# Function to align a single file
# Input: HVite configuration + model name + net file + phonem grammar
#                            + monophone list + mfcc file + format tag
# Output: The lab file corresponding to the segmentation
# deal with silence size
def alignSoloTriphones(mfcc_file, net_file, netflat_file, labPhon_file,
                                              labSenon_file, syn_file, synflat_file,
                                              HViteConf_file, model_file, modelTri_file,
                                              modelTriS_file, phon_file, mono_file, tri_file,
                                              binary_tag, gram_tag, lia_phonetisation_flag,
                                              target_rate, trace):
                                                  
    TARGETRATE    = target_rate
    rec_file      = mfcc_file.replace(".mfcc", ".rec")
    if os.path.exists(rec_file):
        os.remove(rec_file)
    htk_path      = htk_bin_dir()
    if(gram_tag == 0):
        if(trace == True):
            print("-Alignment using HVite by monophones...")
        # perform the alignement using HVite
        subprocess.call(htk_path + "HVite -A -T 1 -o SW -B -y rec -m -C "
                                        + HViteConf_file + " -H " + model_file + " -w " + net_file + " "
                                        + phon_file + " " + mono_file + " " + mfcc_file, shell = True)
    else:
        if(trace == True):
            print("-Alignment using HVite by triphones based on the Bigram-Model..."
                        + "CAN TAKE AGES!!!!\n")
        subprocess.call(htk_path + "HVite -A -T 1 -o SW -B -p -5.0 -s 10.0 -t 100.0 -y "
                                        + "rec -C " + HViteConf_file + " -H " + modelTriS_file + " -w " 
                                        + net_file + " " + phon_file + " " + tri_file + " " + mfcc_file
                                        ,shell = True)
    # move the rec file to labPhon_file
    subprocess.call("mv " + rec_file + " " + labPhon_file, shell = True)
    subprocess.call("cp " + labPhon_file + " " + labSenon_file, shell = True)
    input_tab     = readFile(labPhon_file, trace)
    # check the fronteer values
    split_input_tab       = input_tab.split('\n')
    split_input_tab.pop()
    labsplit_tab          = split_input_tab[0].split(' ')
    num                   = int(labsplit_tab[1])
    val                   = num % TARGETRATE
    if(val != 0):
        if(val == 1):
            labsplit_tab[1]   = str(int(labsplit_tab[1]) - 1)
        else:
            labsplit_tab[1]   = str(int(labsplit_tab[1]) + 1)
        split_input_tab[0]  = labsplit_tab[0] + " " + labsplit_tab[1] + " " \
                                                + labsplit_tab[2]

    labsplit_tab = split_input_tab[len(split_input_tab) - 1].split(' ')
    num =  int(labsplit_tab[1])
    val = num % TARGETRATE
    if( val != 0):
        if(val == 1):
            labsplit_tab[1]   = str(int(labsplit_tab[1]) - 1)
            
        else:
            labsplit_tab[1]   = str(int(labsplit_tab[1]) + 1)
        split_input_tab[len(split_input_tab) - 1] = labsplit_tab[0] + " "   \
                                                                                            + labsplit_tab[1] + " "   \
                                                                                            + labsplit_tab[2]
    aligned_sentence = ""
    for elem in split_input_tab:
        labsplit_tab        = elem.split(' ')
        aligned_sentence    = aligned_sentence + labsplit_tab[2]
                    
    if(trace == True):
        print("alignSolo -> aligned sentence: \t\"" + aligned_sentence + "\"\n")
    output_tab = []
    output_tab.append("( ")
    if(trace == True):
        print("-Check silences duration...\n")
    tabMax = len(split_input_tab)
    sp_last = False
    for count in range(0, tabMax):
        if sp_last == True:
            sp_last = False
            continue
        input_current_line  = split_input_tab[count].split(' ')
        try:
            input_next_line   = split_input_tab[count + 1].split(' ')
        except:
            if(input_current_line[2] == "##"):
                output_tab.append("## ") # A remplacer par ###
            break
        if(input_current_line[2] == "##"):
            output_tab.append("## ") # A remplacer par ###
                        
        elif(input_current_line[2] == "sp"):
            duration          = int(input_current_line[1])                    \
                                                - int(input_current_line[0])
            if (duration < 500000):     
                output_tab.append(input_next_line[2] + " ")
                sp_last = True
            elif (duration < 1000000):
                # remove sp before plosive
                if(input_next_line[2] == "pp" or input_next_line[2] == "tt"
                      or input_next_line[2] == "kk" or input_next_line[2] == "bb"
                      or input_next_line[2] == "dd" or input_next_line[2] == "gg" ):
                    output_tab.append(input_next_line[2] + " ")
                    sp_last = True
                else:
                    output_tab.append("sp ") # A remplacer par #
            else:
                output_tab.append("sp ")
        elif(input_current_line[2] == "br"):
            duration          = int(input_current_line[1])                    \
                                                - int(input_current_line[0])
            if (duration < 500000):
                output_tab.append(input_next_line[2] + " ")
                sp_last = True 
            elif (duration < 1000000):
                # remove br before plosive
                if(input_next_line[2] == "pp" or input_next_line[2] == "tt"
                      or input_next_line[2] == "kk" or input_next_line[2] == "bb"
                      or input_next_line[2] == "dd" or input_next_line[2] == "gg" ):
                    output_tab.append(input_next_line[2] + " ")
                    sp_last = True
                else:
                    output_tab.append("br ") # A remplacer par #
            else:
                output_tab.append("br ")
        else:
            output_tab.append(input_current_line[2] + " ")

    output_tab.append(")\n")
  
    corrected_sentence    = ""
    for elem in output_tab:
        corrected_sentence  = corrected_sentence + elem
                    
    if(trace == True):
        print("alignSolo -> corrected sentence1: \t\""
                    + corrected_sentence + "\"\n")
    #check unique phoneme between silences
    split_corrected_sentence = corrected_sentence.split(" ")
    tabMax = len(split_corrected_sentence)
    record = []
    for count in range (0, tabMax - 2):
        current     = split_corrected_sentence[count]
        next2       = split_corrected_sentence[count + 2]
        if(current == "sp" or current == "br" or current == "##"):
            if(next2 == "sp" or next2 == "br"):
                record.append(next2)
                try:
                    next3         = split_corrected_sentence[count + 3]
                    #in case of two silences
                    if(next3 == "sp" or next3 == "br"):
                        record.append(next3)
                except:
                    pass
    for elem in record:
        split_corrected_sentence.remove(elem)
    
    tabMax = len(split_corrected_sentence)
    corrected_sentence2   = ""
    corrected_sentence2   = corrected_sentence2 + split_corrected_sentence[0]
    for count in range (1, tabMax):
        corrected_sentence2 = corrected_sentence2 + " "                     \
                                                + split_corrected_sentence[count]
    
    output_tab2           = []
    output_tab2.append("(" + corrected_sentence2 + ")")

    if(trace == True):
        print("alignSolo -> corrected sentence2: \t\"" 
                    + corrected_sentence2 + "\"\n")
    remove_if_exists(synflat_file)
    many_writeFile(synflat_file, output_tab2, trace)
    subprocess.call(htk_path + "HParse " + synflat_file + " " + netflat_file, shell = True)
    if(lia_phonetisation_flag == False):
        if(trace == True):
            print("-ReAlignment using HVite by monophones...\n")
        # perform the alignment using HVite
        subprocess.call(htk_path + "HVite -A -T 1 -o SW -B -y rec -m -C "
                + HViteConf_file + " -H " + model_file + " -w " + netflat_file 
                + " " + phon_file + " " + mono_file + " " + mfcc_file, shell = True) 
        # move the rec file to labPhon_file 
        subprocess.call("mv " + rec_file + " " + labPhon_file, shell = True)

    ###
    # Axel:
    # all the following does not seem to have any effect?
    # it may have been used only for verification of the new alignment 
    # by means of comparing the sentence with the realigned sentence?
    #
    # comparision of alignSoloTriphones and alignSoloTriphonesNoRealignment reveals 
    # that the same happened in the perl implementation
    ###
    if(trace == True):
        print("alignSolo -> align at phone level\n")
    #check fronteers value
    lab_tab       = readFile(labPhon_file, trace)
    split_lab_tab = lab_tab.split("\n")
    split_lab_tab.pop()
    labsplit_tab  = split_lab_tab[0].split(" ")
    val           = int(labsplit_tab[1]) % TARGETRATE
    if(val != 0):
        if( val == 1 ):
            labsplit_tab[1]   = str(int(labsplit_tab[1]) - 1)
        else:
            labsplit_tab[1]   = str(int(labsplit_tab[1]) + 1)   
        split_lab_tab[0]    = labsplit_tab[0] + " " + labsplit_tab[1] + " " \
                                                + labsplit_tab[2]
    labsplit_tab          = split_lab_tab[len(split_lab_tab) - 1].split(" ")
    val                   = int(labsplit_tab[1]) % TARGETRATE
    if(val != 0):
        if( val == 1 ):
            labsplit_tab[1]   = str(int(labsplit_tab[1]) - 1  )
        else:
            labsplit_tab[1]   = str(int(labsplit_tab[1]) + 1)
                        
        split_lab_tab[len(split_lab_tab)-  1] = labsplit_tab[0] + " "       \
                                                                                    + labsplit_tab[1] + " "       \
                                                                                    + labsplit_tab[2]
    #write phone file and senone file : 
    phonePrec             = "##"
    labPhon_tab           = []
    labPhon_tab           = "0 "
    labSenon_tab          = []
    count                 = 0
    for elem in split_lab_tab:
        labSplit_tab        = elem.split(" ")
        num                 = labSplit_tab[2]
        phone               = ""
        if(num == "s2"):
            num               = 1
        elif(num == "s3" ):
            num               = 2    
        else:
            num               = 3
        try:
            phone             = labSplit_tab[3]
        except:
            pass
        if(phone != "" and phone != "##"):
            labSplit_tab2     = phone.split('[-\+]')
            phone = labSplit_tab2[1]   
        if(phone != "" and count != 0):
            labPhon_tab       = labPhon_tab + labSplit_tab[0] + " "           \
                                                + phonePrec + "\n " + labSplit_tab[0]
            phonePrec         = phone
        labSenon_tab.append(labSplit_tab[0] + " " + labSplit_tab[1] + " "
                                                  + str(num) + " " + str(phone) + "\n")
        count               = count + 1
    labPhon_tab           = labPhon_tab + labSplit_tab[1] + " ##\n"
    realigned_sentence    = ""
    labPhon_tab_split     = labPhon_tab.split("\n")
    labPhon_tab_split.pop()
    for elem in labPhon_tab_split:
        lab_phon_split      = elem.split(" ")
        realigned_sentence  = realigned_sentence + lab_phon_split[2]
    if(realigned_sentence == "##"):
        realigned_sentence  = ""
    if(trace == True):
        print("alignSolo -> realigned sentence: \t\"" + realigned_sentence + "\"\n")
    
# makeSemiphon
# input : LabSenon file + LabSemiphon path
# output : LabSemiphon file written
def makeSemiphon(labPhon_file, labSemiphon_file, target_rate, trace):
    TARGETRATE                    = target_rate
    if(trace == True):
        print("-Make semiphone...\n")
    input_tab                     = readFile(labPhon_file, trace)
    split_input_tab               = input_tab.split("\n")
    split_input_tab.pop()
    output_tab                    = []
    tabMax = len(split_input_tab)
    sp_last = False
    br_last = False
    for count in range(0, tabMax):
        input_current_line          = split_input_tab[count].split(" ")
        done                        = False
        if(sp_last == True):
            sp_last = False
            continue
        elif(br_last == True):
            br_last = False
            continue
        try:
            if(input_current_line[3] != None):
                if(input_current_line[3] != "##" and input_current_line[3] !=
                      "sp" and input_current_line[3] != "br"):
                    phon_name             = input_current_line[3]
                    startPhon             = input_current_line[0]
                    count                 = count + 1
                    input_next_line       = split_input_tab[count].split(" ")
                    input_next2_line      = split_input_tab[count + 1].split(" ")
                    midPhon               = (int(input_next_line[0]) 
                                                                +  int(input_next_line[1])) / 2
                    #troncature
                    midPhon               = int(midPhon / TARGETRATE) * TARGETRATE
                    endPhon               = input_next2_line[1]

                    output_tab.append(str(startPhon) + " "  + str(midPhon)   + " "
                                                    + str(phon_name) + "\n" 
                                                    + str(midPhon)   + " "  + str(endPhon)   + " "
                                                    + str(phon_name) + "\n")
                    done                  = True
                elif(input_current_line[3] == "sp"):
                    startPhon             = input_current_line[0]
                    input_next_line       = split_input_tab[count + 1].split(" ")
                    while(input_next_line[2] == None):
                        count               = count + 1
                        input_current_line  = split_input_tab[count].split(" ")
                        input_next_line     = split_input_tab[count + 1].split(" ")
                    endPhon               = input_current_line[1]
                    midPhon               = (startPhon + endPhon) / 2
                    #troncature
                    midPhon               = int(int(midPhon) / TARGETRATE) * TARGETRATE
                    output_tab.append(str(startPhon) + " " + str(midPhon) + " sp\n"
                                                        + str(midPhon) + " " + str(endPhon) + " sp\n")
                    done                  = True
                elif(input_current_line[3] == "br"):
                    startPhon             = input_current_line[0]
                    input_next_line       = split_input_tab[count + 1].split(" ")
                    while(input_next_line[2] == None):
                        count               = count + 1
                        input_current_line  = split_input_tab[count].split(" ")
                        input_next_line     = split_input_tab[count + 1].split(" ")
                            
                    endPhon               = input_current_line[1]
                    midPhon               = (startPhon + endPhon) / 2
                    #troncature
                    midPhon               = int(int(midPhon) / TARGETRATE) * TARGETRATE
                    output_tab.append(str(startPhon) + " " + str(midPhon) + " br\n"
                                                        + str(midPhon) + " " + str(endPhon) + " br\n")
                    done          = True
                elif(input_current_line[3] == "##"):
                    startPhon             = input_current_line[0]
                    input_next_line       = split_input_tab[count + 1].split(" ")
                    while(input_next_line[3] != None and count < tabMax - 1):
                        count               = count + 1
                        input_current_line  = split_input_tab[count].split(" ")
                        input_next_line     = split_input_tab[count + 1].split(" ")
                    if(count == tabMax - 1):
                        count               = count + 1
                        input_current_line  = split_input_tab[count].split(" ")  
                    endPhon               = input_current_line[1]
                    output_tab.append(str(startPhon) + " " + str(endPhon) + " ##\n")
                    done                  = True
        except:
            pass
        if(input_current_line[2] == "##" and done == False):
            startPhon                 = input_current_line[0]
            endPhon                   = input_current_line[1]
            output_tab.append(str(startPhon) + " " + str(endPhon) + " ##\n")
            
        elif(done == False):
            startPhon         = input_current_line[0]
            endPhon           = input_current_line[1]
            midPhon           = (int(startPhon) + int(endPhon)) / 2
            output_tab.append(str(startPhon) + " " + str(midPhon) + " "
                                                + input_current_line[2] + "\n" 
                                                + str(midPhon) + " " + str(endPhon) + " "
                                                + input_current_line[2] + "\n")
    many_writeFile(labSemiphon_file, output_tab, trace)
    if(trace == True):
        print("writeFile -> " + labSemiphon_file)
        
# lia2class
# Function to convert liaphon sequence to Major phonetic class sequence
# Input:  string corresponding to the sequence with liaphon format
# Output: string corresponding to the sequence of Major phonetic class
def lia2class(sequence):
    # Adding spaces between liaphon phonems and graphs symbols
    liaphon_graph_with_spaces     = ""
    model                         = re.compile("..")
    if (model.search(sequence) != None):
        liaphon_list = model.findall(sequence)
        for elem in liaphon_list:
            liaphon_graph_with_spaces = liaphon_graph_with_spaces + str(elem) + " "
    sequence                      = liaphon_graph_with_spaces
    # [W@] all vowels (including schwa)
    # [0L] all consonants
    # [GOL] any phonems except vowels
    # [W@GOL] all non silent phonems
    sequence = sequence.replace("in", "W")        # -> e~ (vin, pin)
    sequence = sequence.replace("an", "W")        # -> a~ (cent, vent)
    sequence = sequence.replace("on", "W")        # -> o~ (bon, son)
    sequence = sequence.replace("un", "W")        # -> 9~ (brun)
    sequence = sequence.replace("ii", "W")        # -> i  (si, tic)
    sequence = sequence.replace("ei", "W")        # -> e  (ses, cl'e)
    sequence = sequence.replace("ai", "W")        # -> E  (seize)
    sequence = sequence.replace("aa", "W")        # -> a  (patte)
    sequence = sequence.replace("ah", "W")        # -> A  (pâtes) (à  activer)
    sequence = sequence.replace("oo", "W")        # -> O  (comme, pomme)
    sequence = sequence.replace("au", "W")        # -> o  (gros)
    sequence = sequence.replace("ou", "W")        # -> u  (doux, genou)
    sequence = sequence.replace("uu", "W")        # -> y  (du, mur)
    sequence = sequence.replace("eu", "W")        # -> 2  (deux)
    sequence = sequence.replace("oe", "W")        # -> 9  (neuf, seul)
    sequence = sequence.replace("ee", "@")        # -> @  (justement, nulle)
    sequence = sequence.replace("yy", "G")        # -> j  (ion, bien)
    sequence = sequence.replace("ww", "G")        # -> w  (coin, loin, juin)
    sequence = sequence.replace("uy", "G")        # -> H  (nuit, oui)
    sequence = sequence.replace("pp", "O")        # -> p  (pont)
    sequence = sequence.replace("tt", "O")        # -> t  (temps)
    sequence = sequence.replace("kk", "O")        # -> k  (quand)
    sequence = sequence.replace("bb", "O")        # -> b  (bon)
    sequence = sequence.replace("dd", "O")        # -> d  (dans)
    sequence = sequence.replace("gg", "O")        # -> g  (gant)
    sequence = sequence.replace("ff", "O")        # -> f  (femme)
    sequence = sequence.replace("ss", "O")        # -> s  (sans)
    sequence = sequence.replace("ch", "O")        # -> S  (champ)
    sequence = sequence.replace("vv", "O")        # -> v  (vent)
    sequence = sequence.replace("zz", "O")        # -> z  (zone)
    sequence = sequence.replace("jj", "O")        # -> Z  (gens)
    sequence = sequence.replace("ll", "L")        # -> l  (long, lac)
    sequence = sequence.replace("rr", "L")        # -> R  (rond, renard)
    sequence = sequence.replace("mm", "O")        # -> m  (mont, maman)
    sequence = sequence.replace("nn", "O")        # -> n  (nom)
    sequence = sequence.replace("ng", "O")        # -> N  (camping)
    sequence = sequence.replace("##", "S")        # -> ##
    sequence = sequence.replace("sp", "S")        # -> ##
    sequence = sequence.replace("mp", "S")        # -> ##
    sequence = sequence.replace("br", "S")        # -> ##
    sequence = sequence.replace(" ", "")
    return sequence

#apply_rule 'subfunction only)
# -- Sequential segmentation rules: match pattern -> segmentation
def apply_rule(sequence, left_exp, right_exp):
    a     = sequence
    b     = ""
    m_exp = '(.*' + left_exp + ')(' + right_exp + '.*)'
    model = re.compile(m_exp)
    tmp   = model.search(sequence)
    while(tmp != None):
        a   = tmp.group(1)
        b   = sequence.replace(a, "", 1)
        sequence = a + " " + b
        tmp = model.search(sequence)
    return sequence

# search_pattern (subfunction only)
# Search a pattern in input text and split the text for each pattern 
# Input : input file + rule
# Output: input file with spaces
def search_pattern(input_text, pattern):
    model                 = re.compile(pattern)
    input_text_with_spaces= ""
    if(model.search(input_text) != None):
        find_sequence       = model.findall(input_text)
        for element in find_sequence:
            input_text_with_spaces    = input_text_with_spaces + str(element) + " "
    return input_text_with_spaces

# word_pattern (subfunction only for recoWord)
def word_pattern(input_text, pattern):
    word_pat      = search_pattern (input_text, pattern)
    if(word_pat == ""):
        return None
    elif(word_pat == "## ## "):
        return "##"
    elif(word_pat[1] == word_pat[0]):
        return word_pat[1]
    word          = ""
    for elem in word_pat.split(", "):
        string      = str(elem)
        if(string != ''):
            word      = word + " " + string
    return word
# recoSyllables
# Input : labPhonLIA file
# Output : labPhon file + labSylLIA file + labSyl file
def recoSyllables(labPhonLIA_file, labSylLIA_file, labPhon_file,
                                    labSyl_file, trace):
  
    if(trace == True):
        print("***Syllable recognition...\n")
    labPhonLIA_tab= readFile(labPhonLIA_file, trace)
    labPhonLIA_tab= re.sub("  ", " ", labPhonLIA_tab)
    sentence      = ""
    split_labPhonLIA_tab  = labPhonLIA_tab.split("\n")
    split_labPhonLIA_tab.pop()
    for elem in split_labPhonLIA_tab:
        line        = elem.split(" ")
        # recreate the phonetic sequence
        sentence    = sentence + line[2]

    # Syllable segmentation
    syllabe_segmentation  = lia2class(sentence)
    # --generic groups of phonems
    V = '[W@]'# all vowels (including schwa)
    C = '[OL]'# all consonants
    X = '[GOL]'# any phonems except vowels
    A = '[W@GOL]'# all non silent phonems
    # @C:0,4V -> @ C:0,4V
    syllabe_segmentation  = apply_rule(syllabe_segmentation, '@', 
                                                                                                                        C + '{0,4}' + V)
    # VV -> V + V
    syllabe_segmentation  = apply_rule(syllabe_segmentation, V, V)
    # VXV -> V + XV
    syllabe_segmentation  = apply_rule(syllabe_segmentation, V, X + V)
    # VXGV -> V + XGV
    syllabe_segmentation  = apply_rule(syllabe_segmentation, V, X + 'G' + V)
    # VOLV -> V + OLV
    syllabe_segmentation  = apply_rule(syllabe_segmentation, V, 'OL' + V)
    # VXXV -> VX + XV
    syllabe_segmentation  = apply_rule(syllabe_segmentation, V + X, X + V)
    # VOLGV -> V + OLGV
    syllabe_segmentation  = apply_rule(syllabe_segmentation, V, 'OLG' + V)
    # VXXGV -> VX + XGV
    syllabe_segmentation  = apply_rule(syllabe_segmentation, V + X, X + 'G' + V)
    # VXOLV -> VX + OLV
    syllabe_segmentation  = apply_rule(syllabe_segmentation, V + X, 'OL' + V)
    # VXXXV -> VXX + XV
    syllabe_segmentation  = apply_rule(syllabe_segmentation, V + X + X, X + V)
    # VXOLGV -> VX + OLGV
    syllabe_segmentation  = apply_rule(syllabe_segmentation, V + X, 'OLG' + V)
    # VXXXXV -> VXX + XXV
    syllabe_segmentation  = apply_rule(syllabe_segmentation, V + X + X, 
                                                                                                                      X + X + V)
    # VXXXXGV -> VXX + XXGV
    syllabe_segmentation  = apply_rule(syllabe_segmentation, V + X + X, 
                                                                                                                      X + X + 'G' + V)
    # SA -> S + A
    syllabe_segmentation  = apply_rule(syllabe_segmentation, 'S', A)
    # AS -> A + S
    syllabe_segmentation  = apply_rule(syllabe_segmentation, A, 'S')
    # AS -> A + S
    syllabe_segmentation  = apply_rule(syllabe_segmentation, 'S', 'S')
    
    sentence_with_spaces  = search_pattern(sentence, "..")
    tab_sentence          = sentence_with_spaces.split(' ')
    tab_sentence.pop()
    tab_syllabe_segmentation      = list(syllabe_segmentation)
    tabMax                = len(tab_syllabe_segmentation)
    syllabe_string        = ""
    
    cpt = 0
    for count in range(0, tabMax):
        if tab_syllabe_segmentation[count] == " ":
            syllabe_string    = syllabe_string + " "
        else:
            syllabe_string    = syllabe_string + tab_sentence[cpt]
            cpt = cpt + 1
    syllabe_segmentation  = re.sub("[W@]", "V", syllabe_segmentation)
    syllabe_segmentation  = re.sub("[OLG]", "C", syllabe_segmentation)
    tab_sentence          = syllabe_string.split(' ')
    tab_syllabe_segmentation      = syllabe_segmentation.split(' ')
    
    tabMax                = len(tab_syllabe_segmentation)
    labSyl_tab            = []
    for count in range(0, tabMax):
        labSyl_tab.append(tab_sentence[count] + " "
                                            + tab_syllabe_segmentation[count] + "\n")
    labSylTypeLIA_tab     = ["0 "]
    labSylLIA_tab         = ["0 "]
    indexPhon             = 0
    labPhon_tab           = []
    while(sentence != ""):
        for elem in labSyl_tab:
            # delete spaces repeat
            elem              = elem.replace("  ", " ")
            # remove the last newline
            elem              = elem.replace("\n", "")
            # get the 2 field of lia_text2phon result
            line              = elem.split(" ")
            # find Nucleus Index :
            nucleus_index     = 0
            ONC_tab           = list(line[1])
            if(ONC_tab[0] != "S"):
                while(ONC_tab[nucleus_index] != "V"):
                    nucleus_index = nucleus_index + 1
                    if(nucleus_index > 10):
                        print("Index " + nucleus_index
                                    + "  Syllabation problem on syllabe : " + line)
                        exit(0)
            else:
                nucleus_index   = "S"
            sentence          = sentence.replace(line[0], "", 1)
            phonem            = search_pattern(line[0], "..")
            phonem            = phonem.rstrip()
            tabMax            = indexPhon + len(phonem.split(" "))
            cpt = 0
            split_labPhonLIA_tab      = labPhonLIA_tab.split("\n")
            split_labPhonLIA_tab.pop()
            for count in range(indexPhon, tabMax):
                if(nucleus_index == "S"):
                    phoneType     = "S"   
                elif(cpt < nucleus_index):
                    phoneType     = "O"   
                elif(cpt == nucleus_index):
                    phoneType     = "N"   
                else:
                    phoneType     = "C"
                lab_tab         = split_labPhonLIA_tab[count].split(" ")
                labPhon_tab.append(lab_tab[0] + " " + lab_tab[1] + " "
                                                      + phoneType + "\n")
                cpt             = cpt + 1
            #last syllable's phonem index
            indexPhon         = indexPhon + len(phonem.split(" ")) - 1
            lab_tab           = split_labPhonLIA_tab[indexPhon].split(" ")
            indexPhon         = indexPhon + 1
            if(indexPhon == len(split_labPhonLIA_tab)):
                    labSylLIA_tab.append(lab_tab[1] + " " + line[0] + "\n")
                    labSylTypeLIA_tab.append(lab_tab[1] + " " + line[1] + "\n")
            else:
                labSylLIA_tab.append(lab_tab[1] + " " + line[0] + "\n"
                                                +    lab_tab[1] + " ")
                labSylTypeLIA_tab.append(lab_tab[1] + " " + line[1] + "\n"
                                                +        lab_tab[1] + " ")
    remove_if_exists(labSylLIA_file)
    many_writeFile(labSylLIA_file, labSylLIA_tab, trace)
    remove_if_exists(labSyl_file)
    many_writeFile(labSyl_file, labSylTypeLIA_tab, trace)
    remove_if_exists(labPhon_file)
    many_writeFile(labPhon_file, labPhon_tab, trace)


def recoPhrase(labPhon_file, labPhraseLIA_file, trace):
    if(trace == True):
        print("***Phrase recognition...\n")
    labPhon_tab           = readFile(labPhon_file, trace)
    labPhon_tab           = re.sub("  ", " ", labPhon_tab)
    sentence              = ""
    syllabe_segmentation  = ""
    split_labPhon_tab     = labPhon_tab.split("\n")
    # remove the last newline
    split_labPhon_tab.pop()
    # reading the pipe
    for elem in split_labPhon_tab:
        # get the 2 field of lia_text2phon result
        line = elem.split(" ")
        # recreate the phonetic sequence
        sentence = sentence + line[2]
        syllabe_segmentation= syllabe_segmentation + line[2] + " "
    syllabe_segmentation  = re.sub("##", "|##|", syllabe_segmentation)
    syllabe_segmentation  = re.sub("br", "|br|", syllabe_segmentation)
    syllabe_segmentation  = re.sub("sp", "|sp|", syllabe_segmentation)
    syllabe_segmentation  = re.sub("mp", "|mp|", syllabe_segmentation)
    syllabe_segmentation  = re.sub("\|sp\| \|br\|", "|spbr|",
                                                                  syllabe_segmentation)
    syllabe_segmentation  = re.sub("\|br\| \|sp\|", "|brsp|",
                                                                syllabe_segmentation)
    syllabe_segmentation  = re.sub(" ", "", syllabe_segmentation)
    syllabe_segmentation  = re.sub("\|", " ", syllabe_segmentation)
    syllabe_segmentation  = re.sub("\s\s+", " ", syllabe_segmentation)
    syllabe_segmentation  = re.sub("^ ", "", syllabe_segmentation)
    
    tab_syllabe_segmentation = syllabe_segmentation.split(" ")
    
    labPhrase_tab         = ""
    tabMax                = len(tab_syllabe_segmentation)
    for count in range(0, tabMax):
        labPhrase_tab       = labPhrase_tab + tab_syllabe_segmentation[count] + "\n"
    labPhraseLIA_tab      = []
    labPhraseLIA_tab.append("0 ")
    split_labPhrase_tab   = labPhrase_tab.split("\n")
    indexPhon             = 0
    lab_tab               = []
    
    for elem in split_labPhrase_tab:
        if(sentence == ""):
            break
        elem                = elem.replace("  ", " ")
        elem                = elem.replace("\n", "")
        line                = elem
        sentence            = sentence.replace(line, "", 1)
        phonem              = search_pattern(line, "..")
        phonem              = phonem.rstrip()
        indexPhon           = indexPhon + len(phonem.split(" ")) - 1
        lab_tab             = split_labPhon_tab[indexPhon].split(" ")
        indexPhon           = indexPhon + 1
        tabMax              = len(split_labPhon_tab)
        if(indexPhon == tabMax):
            labPhraseLIA_tab.append(lab_tab[1] + " " + line + "\n")      
        else:
            labPhraseLIA_tab.append(lab_tab[1] + " " + line + "\n"
                                                            + lab_tab[1] + " ")
    remove_if_exists(labPhraseLIA_file)
    many_writeFile(labPhraseLIA_file, labPhraseLIA_tab, trace)

# recoWords
# Recognize words and write word and grammar lab file 
# input regexp_name, lab_file and format_tag
# output word and grammar lab_file
def recoWords(labPhon_file, labWord_file, labPhrase_file,
                            labWordLIA_file, labGram_file, regexp_tab, words_tag, trace): 
    if(trace == True):
        print("***Word recognition...\n")
        print("-recognizing pronunciation...\n")
    labPhon_tab           = readFile(labPhon_file, trace)
    regtmp_tab            = regexp_tab
    split_labPhon_tab     = labPhon_tab.split("\n")
        # remove the empty last newline
    split_labPhon_tab.pop()
    sentence              = ""
    for elem in split_labPhon_tab:
        # delete spaces repeat
        elem                = elem.replace("  ", " ")
        # get the 2 field of lia_text2phon result
        line                = elem.split(" ")
        # recreate the phonetic sequence
        sentence            = sentence + line[2]
    # Build general regexp
    if trace == True :
            print("sentence", sentence)
    reg_sentence = ""
    for elem in regexp_tab:
        print(elem)
        elem                = elem.replace("  ", " ")
        elem                = elem.replace("\n", "")
        line                = elem.split(" ")
        reg                 = line[0] + "(?:sp)?" + "(?:br)?" + "(?:spbr)?" \
                                                                    + "(?:brsp)?"
        if(words_tag == 2):
            reg_sentence      = reg_sentence + "(?:(" + reg + ")*)"
        elif(words_tag == 1):
            reg_sentence      = reg_sentence + "(?:(" + reg + ")?)"
        else:
            reg_sentence      = reg_sentence + "(" + reg + ")"

    if trace == True :
            print("reg_sentence", reg_sentence)
    # Build pron tab
    pron_tab              = []
    word_pat              = search_pattern(sentence, reg_sentence)
    word                  = []
    for elem in word_pat.split(", "):
        string              = str(elem)
        string              = string.replace('(\'', '')
        string              = string.replace('\') ', '')
        string              = string.replace('\'', '')
        if(string != ''):
            word.append(string)
    if(word != []):
        tabMax              = len(regexp_tab)
        for count in range(0, tabMax):
            line              = regexp_tab[count].split(" ")
            try:
                if(line[3] != None):
                    pron_tab.append(word[count] + " " + line[1] + " "
                                                + line[2]     + " " + line[3])
            except:
                pron_tab.append(word[count]   + " " + line[1] + " " + line[2])
                
    else:
        print("problem recoWord!!!!! \n" + sentence + "\n" + reg_sentence + "\n")
        exit(0)
    
    tabMax                = len(pron_tab)
    split_pron_tab        = pron_tab[tabMax -1].split(" ")
    
    if(split_pron_tab[0] != "##"):
        exit(0)
    wordIn_tab            = []
    for count in range(0, tabMax):
        split_pron_tab      = pron_tab[count].split(" ")
        wordIn_tab.append(split_pron_tab[1])
    tabMax                = len(regtmp_tab)
    cpt                   =  0
    wordTot_tab           = []
    wordOut_tab           = []
    for count in range(0, tabMax):
        split_pron_tab      = regtmp_tab[count].split(" ")
        wordTot_tab.append(split_pron_tab[1])
        if(wordTot_tab[count] != wordIn_tab[cpt]):
            wordOut_tab.append(wordTot_tab[count])
        else:
            cpt               = cpt + 1

    if(trace == 1):
        print("recoWords -> word sequence:\t " + str(word) + "\n")
    # Decoding::alignSolo -> align at phone level
    
    labWord_tab           = ["0 "]
    labWordLIA_tab        = ["0 "]
    labGram_tab           = ["0 "]
    if(trace == True):
        print("-Finding the fronteers...\n")
    indexPhon = 0
    for elem in pron_tab :
        lab_tab             = split_labPhon_tab[indexPhon].split(" ")
        elem                = elem.replace("  ", " ")
        elem                = elem.rstrip()
        line                = elem.split(" ")
        line[0]             = line[0].replace("spbr", "")
        line[0]             = line[0].replace("brsp", "")
        line[0]             = re.sub("sp$", "", line[0])
        line[0]             = re.sub("br$", "", line[0])
        tag                 = 1
        # case with sp
        tabMax = len(line[0] + "brsp")
        word                = word_pattern(sentence[0:tabMax], line[0] + "brsp")
        if(word != None):
            sentence          = sentence.replace(line[0] + "brsp", "", 1)
            # read word phoneme
            phonem_list       = search_pattern(line[0], "..")
            phonem            = phonem_list.split(" ")
            for count in range(0, phonem.count('')):
                phonem.remove('')
            indexPhon         = indexPhon + len(phonem) - 1
            split_labPhon_tab= labPhon_tab.split("\n")
            split_labPhon_tab.pop()
            lab_tab           = split_labPhon_tab[indexPhon].split(" ")
            #JUMP "BR" and "SP" Word
            indexPhon         = indexPhon + 2
            labWord_tab.append(lab_tab[1] + " " + line[1] + "\n"
                                                +lab_tab[1] + " ") 
            labWordLIA_tab.append(lab_tab[1] + " " + line[0] + "\n"
                                                +   lab_tab[1] + " ")
            labGram_tab.append(lab_tab[1] + " " + line[2] + "\n"
                                                +lab_tab[1] + " ")
            split_labPhon_tab= labPhon_tab.split("\n")
            split_labPhon_tab.pop()
            lab_tab           = split_labPhon_tab[indexPhon].split(" ")
            indexPhon         = indexPhon + 1
            labWord_tab.append(lab_tab[1] + " /break" + "\n" + lab_tab[1] + " ")
            labWordLIA_tab.append(lab_tab[1] + " brsp" + "\n" + lab_tab[1] + " ")
            try:
                if(line[3] != None):
                    labGram_tab.append(lab_tab[1] + " " + line[3] + "\n"
                                                +    lab_tab[1] + " ")
            except:
                labGram_tab.append(lab_tab[1] + " [YPFAI]" + "\n" + lab_tab[1] + " ")
            tag               = 0
        # case with mp
        if(tag == 1):
            tabMax = len(line[0] + "spbr")
            word              = word_pattern(sentence[0:tabMax], line[0] + "spbr")
            if(word != None):
                sentence        = sentence.replace(line[0] + "spbr", "", 1)
                # read word phoneme
                phonem_list     = search_pattern(line[0], "..")
                phonem          = phonem_list.split(" ")
                for count in range(0, phonem.count('')):
                    phonem.remove('')
                indexPhon       = indexPhon + len(phonem) - 1
                split_labPhon_tab= labPhon_tab.split("\n")
                split_labPhon_tab.pop()
                lab_tab         = split_labPhon_tab[indexPhon].split(" ")
                #JUMP "SP" and "BR" Word
                indexPhon       = indexPhon + 2
                labWord_tab.append(lab_tab[1] + " " + line[1] + "\n"
                                                +  lab_tab[1] + " ") 
                labWordLIA_tab.append(lab_tab[1] + " " + line[0] + "\n"
                                                +     lab_tab[1] + " ")
                labGram_tab.append(lab_tab[1] + " " + line[2] + "\n"
                                                +  lab_tab[1] + " ")
                split_labPhon_tab= labPhon_tab.split("\n")
                split_labPhon_tab.pop()
                lab_tab         = split_labPhon_tab[indexPhon].split(" ")
                indexPhon       = indexPhon + 1
                labWord_tab.append(lab_tab[1] + " /break" + "\n" + lab_tab[1] + " ")
                labWordLIA_tab.append(lab_tab[1] + " spbr" + "\n" + lab_tab[1] + " ")
                try:
                    if(line[3] != None):
                        labGram_tab.append(lab_tab[1] + " " + line[3] + "\n"
                                                +      lab_tab[1] + " ")
                except:
                    labGram_tab.append(lab_tab[1] + " [YPFAI]" + "\n" + lab_tab[1] + " ")
                tag             = 0
        if(tag == 1):
            tabMax = len(line[0] + "sp")
            word              = word_pattern(sentence[0:tabMax], line[0] + "sp")
            if(word != None):
                sentence        = sentence.replace(line[0] + "sp", "", 1)
                word            = word.replace("sp", "", 1)
                # read word phoneme
                phonem_list     = search_pattern(line[0], "..")
                phonem          = phonem_list.split(" ")
                for count in range(0, phonem.count('')):
                    phonem.remove('')
                indexPhon       = indexPhon + len(phonem) - 1
                split_labPhon_tab= labPhon_tab.split("\n")
                split_labPhon_tab.pop()
                lab_tab         = split_labPhon_tab[indexPhon].split(" ")
                #JUMP "SP" Word
                indexPhon       = indexPhon + 1
                labWord_tab.append(lab_tab[1] + " " + line[1] + "\n"
                                                +  lab_tab[1] + " ") 
                labWordLIA_tab.append(lab_tab[1] + " " + line[0] + "\n"
                                                +     lab_tab[1] + " ")
                labGram_tab.append(lab_tab[1] + " " + line[2] + "\n"
                                                +  lab_tab[1] + " ")
                split_labPhon_tab= labPhon_tab.split("\n")
                split_labPhon_tab.pop()
                lab_tab         = split_labPhon_tab[indexPhon].split(" ")
                indexPhon       = indexPhon + 1
                labWord_tab.append(lab_tab[1] + " /break" + "\n" + lab_tab[1] + " ")
                labWordLIA_tab.append(lab_tab[1] + " sp" + "\n" + lab_tab[1] + " ")
                try:
                    if(line[3] != None):
                        labGram_tab.append(lab_tab[1] + " " + line[3] + "\n"
                                                +      lab_tab[1] + " ")
                except:
                    labGram_tab.append(lab_tab[1] + " [YPFAI]" + "\n" + lab_tab[1] + " ")
                tag             = 0
        # case with mp
        if(tag == 1):
            tabMax = len(line[0] + "br")
            word              = word_pattern(sentence[0:tabMax], line[0] + "br")
            if(word != None):
                sentence        = sentence.replace(line[0] + "br", "", 1)
                # read word phoneme
                phonem_list     = search_pattern(line[0], "..")
                phonem          = phonem_list.split(" ")
                for count in range(0, phonem.count('')):
                    phonem.remove('')
                indexPhon       = indexPhon + len(phonem) - 1
                split_labPhon_tab= labPhon_tab.split("\n")
                split_labPhon_tab.pop()
                lab_tab         = split_labPhon_tab[indexPhon].split(" ")
                #JUMP "BR" Word
                indexPhon       = indexPhon + 1
                labWord_tab.append(lab_tab[1] + " " + line[1] + "\n"
                                                +  lab_tab[1] + " ") 
                labWordLIA_tab.append(lab_tab[1] + " " + line[0] + "\n"
                                                +     lab_tab[1] + " ")
                labGram_tab.append(lab_tab[1] + " " + line[2] + "\n"
                                                +  lab_tab[1] + " ")
                lab_tab         = split_labPhon_tab[indexPhon].split(" ")
                indexPhon       = indexPhon + 1
                labWord_tab.append(lab_tab[1] + " /break" + "\n" + lab_tab[1] + " ")
                labWordLIA_tab.append(lab_tab[1] + " br" + "\n" + lab_tab[1] + " ")
                try:
                    if(line[3] != None):
                        labGram_tab.append(lab_tab[1] + " " + line[3] + "\n"
                                                +      lab_tab[1] + " ")
                except:
                    labGram_tab.append(lab_tab[1] + " [YPFAI]" + "\n" + lab_tab[1] + " ")
                tag             = 0
                
        if(tag == 1):
            sentence          = sentence.replace(line[0], "", 1)
            # read word phoneme
            phonem_list       = search_pattern(line[0], "..")
            phonem            = phonem_list.split(" ")
            for count in range(0, phonem.count('')):
                phonem.remove('')
            indexPhon         = indexPhon + len(phonem) - 1
            split_labPhon_tab = labPhon_tab.split("\n")
            split_labPhon_tab.pop()
            lab_tab           = split_labPhon_tab[indexPhon].split(" ")
            indexPhon         = indexPhon + 1
            if(line[1] == "</s>"):
                labWord_tab.append(lab_tab[1] + " " + line[1] + "\n") 
                labWordLIA_tab.append(lab_tab[1] + " " + line[0] + "\n")
                labGram_tab.append(lab_tab[1] + " " + line[2] + "\n")
            else:
                labWord_tab.append(lab_tab[1] + " " + line[1] + "\n"
                                                +  lab_tab[1] + " ") 
                labWordLIA_tab.append(lab_tab[1] + " " + line[0] + "\n"
                                                +     lab_tab[1] + " ")
                labGram_tab.append(lab_tab[1] + " " + line[2] + "\n"
                                                +  lab_tab[1] + " ")
            tag               = 0
    remove_if_exists(labWord_file)
    remove_if_exists(labWordLIA_file)
    remove_if_exists(labGram_file)
    many_writeFile(labWord_file, labWord_tab, trace)
    many_writeFile(labWordLIA_file, labWordLIA_tab, trace)
    many_writeFile(labGram_file, labGram_tab, trace)
    
    labWord_tab2          = readFile(labWord_file, trace)
    phrase                = ""
    split_labWord_tab2    = labWord_tab2.split("\n")
    split_labWord_tab2.pop()
    labPhrase_tab         = []
    for elem in split_labWord_tab2:
        line                = elem.split(" ")
        if(line[2] == "<s>"):
            labPhrase_tab.append(line[0] + " " + line[1] + " " + line[2]
                                    + "\n" + line[1] + " ")
        elif(line[2] == "/break"):
            phrase            = phrase.rstrip()
            labPhrase_tab.append(line[0] + " " + phrase + "\n" + line[0] + " "
                                                +  line[1]       +   " /break\n" + line[1] + " ")
            phrase            = ""
        elif(line[2] == "</s>"):
            phrase            = phrase.rstrip()
            labPhrase_tab.append(line[0] + " " + phrase + "\n" + line[0] + " "
                                                +  line[1] + " " + line[2]+ "\n")
        else:
                phrase          = phrase + line[2] + " "
    remove_if_exists(labPhrase_file)
    many_writeFile(labPhrase_file, labPhrase_tab, trace)

# lia2htk
# Function to convert liaphon sequence to HTK sequence
# Input:  string corresponding to the sequence with liaphon format
# Output: string corresponding to the sequence with HTK format
def lia2XSampa(sequence):
    # Adding spaces between liaphon phonems and graphs symbols
    model                         = re.compile("..")
    liaphon_graph_with_spaces     = ""
    if(model.search(sequence) != None):
        liaphon_list                = model.findall(sequence)
        for elem in liaphon_list:
            liaphon_graph_with_spaces = liaphon_graph_with_spaces + str(elem) + " "
    sequence                      = liaphon_graph_with_spaces
    sequence = sequence.replace("in", "e~")       # -> e~ (vin, pin)
    sequence = sequence.replace("an", "a~")       # -> a~ (cent, vent)
    sequence = sequence.replace("on", "o~")       # -> o~ (bon, son)
    sequence = sequence.replace("un", "9~")       # -> 9~ (brun)
    sequence = sequence.replace("ii", "i")        # -> i  (si, tic)
    sequence = sequence.replace("ei", "e")        # -> e  (ses, cl'e)
    sequence = sequence.replace("ai", "E")        # -> E  (seize)
    sequence = sequence.replace("aa", "a")        # -> a  (patte)
    sequence = sequence.replace("ah", "A")        # -> A  (pâtes) (à  activer)
    sequence = sequence.replace("oo", "O")        # -> O  (comme, pomme)
    sequence = sequence.replace("au", "o")        # -> o  (gros)
    sequence = sequence.replace("ou", "u")        # -> u  (doux, genou)
    sequence = sequence.replace("uu", "y")        # -> y  (du, mur)
    sequence = sequence.replace("eu", "2")        # -> 2  (deux)
    sequence = sequence.replace("oe", "9")        # -> 9  (neuf, seul)
    sequence = sequence.replace("ee", "@")        # -> @  (justement, nulle)
    sequence = sequence.replace("yy", "j")        # -> j  (ion, bien)
    sequence = sequence.replace("ww", "w")        # -> w  (coin, loin, juin)
    sequence = sequence.replace("uy", "H")        # -> H  (nuit, oui)
    sequence = sequence.replace("pp", "p")        # -> p  (pont)
    sequence = sequence.replace("tt", "t")        # -> t  (temps)
    sequence = sequence.replace("kk", "k")        # -> k  (quand)
    sequence = sequence.replace("bb", "b")        # -> b  (bon)
    sequence = sequence.replace("dd", "d")        # -> d  (dans)
    sequence = sequence.replace("gg", "g")        # -> g  (gant)
    sequence = sequence.replace("ff", "f")        # -> f  (femme)
    sequence = sequence.replace("ss", "s")        # -> s  (sans)
    sequence = sequence.replace("ch", "S")        # -> S  (champ)
    sequence = sequence.replace("vv", "v")        # -> v  (vent)
    sequence = sequence.replace("zz", "z")        # -> z  (zone)
    sequence = sequence.replace("jj", "Z")        # -> Z  (gens)
    sequence = sequence.replace("ll", "l")        # -> l  (long, lac)
    sequence = sequence.replace("rr", "R")        # -> R  (rond, renard)
    sequence = sequence.replace("mm", "m")        # -> m  (mont, maman)
    sequence = sequence.replace("nn", "n")        # -> n  (nom)
    sequence = sequence.replace("ng", "N")        # -> N  (camping)
    sequence = sequence.replace("##", "###")      # -> ##
    sequence = sequence.replace("sp", "##")       # -> ##
    sequence = sequence.replace("mp", "#")        # -> ##
    sequence = sequence.replace("br", "°")        # -> ##
    sequence = replace_last(sequence, " ", "") 
    return sequence

def convertFormat(lab_file, labConv_file, trace):
    if(trace == True):
        print("convertFormat ->  " + lab_file)
    lab_tab       = readFile(lab_file, trace)
    split_lab_tab = lab_tab.split("\n")
    split_lab_tab.pop()
    labConv_tab   = []
    for elem in split_lab_tab:
        labsplit_tab= elem.split(" ")
        start       = labsplit_tab[0]
        end         = labsplit_tab[1]
        label       = lia2XSampa(labsplit_tab[2])
        labConv_tab.append(start + " " + end + " " + label + "\n")
    remove_if_exists(labConv_file)
    many_writeFile(labConv_file, labConv_tab, trace)
    
def convertTime(lab_file, labConv_file, trace):
    if(trace == True):
        print("convertTime ->  " + lab_file)
    lab_tab       = readFile(lab_file, trace)
    lab_tab       = readFile(lab_file, trace)
    split_lab_tab = lab_tab.split("\n")
    split_lab_tab.pop()
    labConv_tab   = []
    for elem in split_lab_tab:
        labsplit_tab= elem.split(" ")
        start       = floor(float(labsplit_tab[0]) / 10) / 1000000
        if(start == int(start)):
            start     = str(int(start))
        else:
            start     = str(start)
        end = floor(float(labsplit_tab[1]) / 10) /1000000
        if(end == int(end)):
            end       = str(int(end))
        else:
            end       = str(end)
        label       = labsplit_tab[2]
        labConv_tab.append(start + " " + end + " ")
        tabMax      = len(labsplit_tab)
        for count in range(2, tabMax - 1):
            labConv_tab.append(labsplit_tab[count] + " ")
        labConv_tab.append(labsplit_tab[tabMax - 1] + "\n")
    remove_if_exists(labConv_file)
    many_writeFile(labConv_file, labConv_tab, trace)
