#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Mon Jun 24 16:57:06 2024

@author: takeshi-s

ref:
    https://stackoverflow.com/questions/59198008/how-to-find-all-synoyms-for-a-french-word-using-nltk
    https://stackoverflow.com/questions/8077641/how-to-get-the-wordnet-synset-given-an-offset-id
    wordnet usage: https://www.nltk.org/howto/wordnet.html
    synset dictionary: http://wordnet.pt/synset/00156601-v
    
    
TODO!
    
    Check NP VP correctness by Lucie
    Integrate in Meaninig Miner through ProcessBuilder
    If slow, make server and client to separate initialization step and excution step

"""
# from matplotlib import pyplot as plt
# from pathlib import Path
# from tqdm import tqdm
import pprint as pp
# import pandas as pd
# import numpy as np
# import traceback
# import shutil
# import math
import time
import csv
import sys
import os

# import nltk
# from nltk.tokenize import TreebankWordTokenizer
# from nltk.wsd import lesk
# from nltk.corpus import wordnet as wn

import nltk.langnames as lgn
from langdetect import detect as detect_lang

# import textacy

# from nltk.chunk

"""
python -m pip install nltk
python -m pip install langdetect
python -c "import nltk;nltk.download('wordnet')"
python -c "import nltk;nltk.download('omw-1.4')"
python -c "import nltk;nltk.download('bcp47')"

python -c "import nltk;nltk.download('punkt')"
python -c "import nltk;nltk.download('averaged_perceptron_tagger')"
"""

import spacy
import subprocess
from spacy.matcher import Matcher
from spacy.util import filter_spans

from numba import jit

import warnings
warnings.simplefilter('ignore')

import xml.etree.ElementTree as ET

import socket
import pickle

import base64

def main():
    "Main function"

    HOST = 'localhost'
    PORT = 3150
    BUFSIZE = 4096
    
    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server.bind(("", PORT))
    server.listen()

    lang = None
    lang = 'fr'
    
    # if lang == None:

    #     s_detect_time = time.time()
    #     #input: sentence string, output: language code (e.g. "fr")
    #     lang_code_2 = detect_lang(src_sentence)
    #     e_detect_time = time.time()
    #     print('Time detect: {}'.format(e_detect_time - s_detect_time))
    
    # else:
    #     lang_code_2 = lang
    
    lang_code_2 = lang
    
    lang_name = lgn.langname(lang_code_2)
    lang_code_3 = lgn.langcode(lang_name, typ = 3)
    
    print('lang_name =', lang_name)
    print('lang_code_2 =', lang_code_2)
    print('lang_code_3 =', lang_code_3)    


    # xml2csv()
    # load_csv()
    
    # sent2synset(src_sentence, lang_code_3)
    
    print('### Preparation phase ###')
    nlp = prepare_spacy_model(lang_code_2)
    imageschema_dict = prepare_french_imageschema_dict()
    print('#########################')

    while True:
        client, addr = server.accept()
        client.sendall(b'Hi')
        src_data = client.recv(BUFSIZE)
        src_data = src_data.decode('UTF-8')
        print(src_data)
        
        if src_data == 'kill':
            break
        
        src_sentence = src_data
    
        print(src_sentence)
    
        token_list, lemma_list, pos_list, chunk_tag_list = sent2chunk(nlp, src_sentence, lang_code_2)
        gesture_ID_list, gesture_lemma_list = get_gesture_ID_list(imageschema_dict, lemma_list, pos_list)
        
        # for gesture_ID, gesture_lemma in zip(gesture_ID_list, gesture_lemma_list):
        #     print(gesture_ID, gesture_lemma)
        
        result_list = [token_list, lemma_list, pos_list, chunk_tag_list, gesture_ID_list, gesture_lemma_list]
        
        result = pickle.dumps(result_list)
        
        result = base64.b64encode(result).decode()
        result = result.encode('UTF-8')
        
        client.send(result)
        client.close()
    
    server.close()
    print('Meaning Miner server killed.')

def translate_lemma_to_french(src_lemma_list, src_pos_list):
    
    encoding = 'utf-8'
    lemma_trans_dict_path = r'lemma_trans_dict_en2fr.csv'    
    lemma_trans_csv = load_csv(lemma_trans_dict_path)[1:]
    
    lemma_trans_dict = {}
    for i in range(len(lemma_trans_csv)):
        lemma_trans_dict[(lemma_trans_csv[i][1], lemma_trans_csv[i][2])] = lemma_trans_csv[i][3]
    # pp.pprint(lemma_trans_dict)
    # sys.exit()
    
    # lemma_trans_dict = {
    #                     ('VERB', 'increase'): 'augmenter', 
    #                     ('NOUN', 'left'): 'gauche'
    #                     }
    
    for i in range(len(src_lemma_list)):
        if (src_pos_list[i], src_lemma_list[i]) in lemma_trans_dict.keys():
            src_lemma_list[i] = lemma_trans_dict[(src_pos_list[i], src_lemma_list[i])]
        else:
            src_lemma_list[i] = None
    
    return src_lemma_list


def get_gesture_ID_list(imageschema_dict, lemma_list, pos_list):
    
    s_gesture_time = time.time()
    
    gesture_ID_list = ['_' for x in range(len(lemma_list))]
    gesture_lemma_list = ['_' for x in range(len(lemma_list))]
    
    for schema_type_index in range(len(imageschema_dict)):
        # print(imageschema_dict[schema_type_index].tag)
        # print(schema_type.attrib)
        for synset_index in range(len(imageschema_dict[schema_type_index])):
            # print(synset.tag)
            # print(synset.attrib)
            gesture_ID = imageschema_dict[schema_type_index][synset_index].attrib['id']
            gesture_pos = imageschema_dict[schema_type_index][synset_index].attrib['type']
            gesture_lemma = imageschema_dict[schema_type_index][synset_index].attrib['lemma']
            
            for src_index in range(len(lemma_list)):
                if (lemma_list[src_index] == gesture_lemma) and (pos_list[src_index] == gesture_pos):
                    _, gesture_ID, _ = gesture_ID.split('-')
                    gesture_ID_list[src_index] = gesture_ID
                    gesture_lemma_list[src_index] = gesture_lemma
                    print('Gesture: ID - {}, Lemma - {}'.format(gesture_ID, gesture_lemma))
    
    e_gesture_time = time.time()
    
    print('Time gesture: {}'.format(e_gesture_time - s_gesture_time))
    
    return gesture_ID_list, gesture_lemma_list

def xml2csv():
    
    xml_path = r"C:\Users\takes\Documents\NetBeansProjects\greta-gpl-grimaldi\greta-gpl-grimaldi\bin\Common\Data\MeaningMiner\imageschemaSynset.xml"
    csv_path = r'C:\Users\takes\Documents\Python_script\shared\lemma_trans_dict_fr2en.csv'
    
    out_list = [['POS', 'src_lemma', 'tgt_lemma']]

    tree = ET.parse(xml_path)
    imageschema_dict = tree.getroot()
    
    for schema_type_index in range(len(imageschema_dict)):
        # print(imageschema_dict[schema_type_index].tag)
        # print(schema_type.attrib)
        for synset_index in range(len(imageschema_dict[schema_type_index])):
            # print(synset.tag)
            # print(synset.attrib)
            gesture_ID = imageschema_dict[schema_type_index][synset_index].attrib['id']
            gesture_pos = imageschema_dict[schema_type_index][synset_index].attrib['type']
            gesture_lemma = imageschema_dict[schema_type_index][synset_index].attrib['lemma']
        
            out_list.append([gesture_pos, gesture_lemma, ''])
    
    with open(csv_path, 'w', encoding = 'utf-8') as f:
        writer = csv.writer(f)
        writer.writerows(out_list)

def load_csv(path):
    csv_path = path
    with open(csv_path, encoding = 'utf-8') as f:
        reader = csv.reader(f)
        data = [x for x in reader if x != ['', '', '']]
    # pp.pprint(data)
    return data

def prepare_french_imageschema_dict():
    
    dict_path = r"..\imageschemaSynset.xml"
    
    tree = ET.parse(dict_path)
    imageschema_dict = tree.getroot()
    
    for schema_type_index in range(len(imageschema_dict)):
        # print(imageschema_dict[schema_type_index].tag)
        # print(schema_type.attrib)
        for synset_index in range(len(imageschema_dict[schema_type_index])):
            # print(synset.tag)
            # print(synset.attrib)
            gesture_ID = imageschema_dict[schema_type_index][synset_index].attrib['id']
            gesture_pos = imageschema_dict[schema_type_index][synset_index].attrib['type']
            gesture_lemma = imageschema_dict[schema_type_index][synset_index].attrib['lemma']
            
            gesture_lemma = translate_lemma_to_french([gesture_lemma], [gesture_pos])
            gesture_lemma = gesture_lemma[0]
            
            imageschema_dict[schema_type_index][synset_index].attrib['lemma'] = gesture_lemma
            
            # print(gesture_ID, gesture_pos, gesture_lemma)
    
    return imageschema_dict

def prepare_spacy_model(lang_code_2):
    
    # lang = "fr"
    # lang = 'en'
    lang = lang_code_2
    # pipeline = ["tok2vec", "tagger", "parser", "ner", "attribute_ruler", "lemmatizer"]
    # pipeline = ["tokenizer", "tagger", "parser", "ner", "attribute_ruler", "lemmatizer"]
    
    # cls = spacy.util.get_lang_class(lang)  # 1. Get Language class, e.g. English
    # nlp = cls()                            # 2. Initialize it
    # for name in pipeline:
    #     nlp.add_pipe(name, config={...})   # 3. Add the component to the pipeline
        
    # # doc = nlp.make_doc(sentence)
    # # # doc = nlp(doc)
    # # for name, proc in nlp.pipeline:
    # #     doc = proc(doc)
    # doc = nlp(sentence)
    # print(doc)
    
    try:
        try:
            #faster
            # model_name = "{}_core_news_sm".format(lang)
            model_name = lang + "_core_web_sm"
            # model_name = lang + "_core_web_md"
            # model_name = lang + "_core_web_lg"
            print('Trying to load ' + model_name)
            nlp = spacy.load(model_name)
        except:
            model_name = lang + "_core_news_sm"
            # model_name = lang + "_core_news_md"
            # model_name = lang + "_core_news_lg"
            print('Trying to load ' + model_name)
            nlp = spacy.load(model_name)
    except:
        try:
            model_name = lang + "_core_news_sm"
            # model_name = lang + "_core_news_md"
            # model_name = lang + "_core_news_lg"
            subprocess.call(["python", "-m", "spacy", "download", model_name])
        except:
            model_name = lang + "_core_web_sm"
            # model_name = lang + "_core_web_md"
            # model_name = lang + "_core_web_lg"
            subprocess.call(["python", "-m", "spacy", "download", model_name])
        nlp = spacy.load(model_name)
        
    return nlp

def sent2chunk(nlp, sentence, lang_code_2):        

    #more accurate
    # model_name = "{}_core_news_sm".format(lang)   
    
    doc = nlp(sentence)
    # pp.pprint(dir(doc))
    # for token in doc:
    #     # print(dir(token))
    #     print(token.text, token.lemma_, token.pos_, token.head, token.dep_)
    
    s_chunk_time = time.time()
        
    chunk_tag_list = ['O' for token in doc]
    
    verb_phrase_list = get_verb_phrases(nlp, doc)
    for verb_phrase in verb_phrase_list:
        print('VP[{:02d}:{:02d}]: {}'.format(verb_phrase.start, verb_phrase.end, verb_phrase.text))
        chunk_tag_list = update_chunk_tag(chunk_tag_list, "VP", verb_phrase.start, verb_phrase.end)

    noun_phrase_list = get_noun_phrases(nlp, doc)
    for n_chunk in doc.noun_chunks:
        print('NP[{:02d}:{:02d}]: {} (root: {}, root.head: {}, root.head.pos_: {})'.format(n_chunk.start, n_chunk.end, n_chunk.text, n_chunk.root, n_chunk.root.head, n_chunk.root.head.pos_))
        chunk_tag_list = update_chunk_tag(chunk_tag_list, "NP", n_chunk.start, n_chunk.end)        

    
    token_list  = [token.text for token in doc]
    lemma_list  = [token.lemma_ for token in doc]
    pos_list    = [token.pos_ for token in doc] #e.g. VERB
    tag_list    = [token.tag_ for token in doc] #e.g. VBG
    
    for i in range(len(token_list)):
        print('Token: {:10s}, Lemma: {:10s}, POS: {:10s}, TAG: {:10s}, Chunk_tag: {:10s}'.format(token_list[i], lemma_list[i], pos_list[i], tag_list[i], chunk_tag_list[i]))
        
    e_chunk_time = time.time()
    print('Time chunk {}'.format(e_chunk_time - s_chunk_time))
    
    return token_list, lemma_list, pos_list, chunk_tag_list

def get_noun_phrases(nlp, doc):
    
    return doc.noun_chunks

def get_verb_phrases(nlp, doc):

    # #TODO!: this might not work SOV language (e.g. Japanese)
    # pattern = r'(<VERB>?<ADV>*<VERB>+)'
    # # verb_phrase_list = textacy.extract.pos_regex_matches(doc, pattern)
    # verb_phrase_list = textacy.extract.matches(doc, pattern)
    # for chunk in verb_phrase_list:
    #     print("VP:", chunk.text)
        
    matcher = Matcher(nlp.vocab)
    # pattern = [
    #     [{"POS": "AUX"}, {"POS": "VERB"}],
    #     [{"POS": "VERB"}]
    # ]
    # pattern = [[
    #     {'POS': 'VERB', 'OP': '?'},
    #     {'POS': 'ADV', 'OP': '*'},
    #     {'POS': 'AUX', 'OP': '*'},
    #     {'POS': 'VERB', 'OP': '+'}
    #     ]]
    
    pattern = [
            [
            {'POS': 'VERB', 'OP': '?'},
            {'POS': 'ADV', 'OP': '*'},
            {'POS': 'AUX', 'OP': '*'},
            {'POS': 'VERB', 'OP': '+'}
            ]
    ]
    
    matcher.add("verb-phrases", pattern)
    match_list = matcher(doc)
    match_list = [doc[match[1]:match[2]] for match in match_list]
    match_list = filter_spans(match_list)
    
    return match_list
    
def update_chunk_tag(chunk_tag_list, chunk_type, start_i, end_i):
    
    for i in range(start_i, end_i):
        if i == start_i:
            chunk_tag_list[i] = 'B-{}'.format(chunk_type)
        else:
            chunk_tag_list[i] = 'I-{}'.format(chunk_type)
    return chunk_tag_list

# @jit(forceobj=True)
# def sent2synset(src_sentence, lang_code_3):

#     #######################################
    
#     # from nltk.corpus import wordnet as wn
    
#     # ['als', 'arb', 'cat', 'cmn', 'dan', 'eng', 'eus', 'fas',
#     # 'fin', 'fra', 'fre', 'glg', 'heb', 'ind', 'ita', 'jpn', 'nno',
#     # 'nob', 'pol', 'por', 'spa', 'tha', 'zsm']
#     # lang='fra'
    
#     #TODO!
#     # lang_dictionary = {'fr': 'fra', 'en': 'eng', 'ja': 'jpn'}    
    
#     tokenizer = TreebankWordTokenizer()

#     s_synset_time = time.time()
    
#     sent = tokenizer.tokenize(src_sentence)
#     # synsets = [lesk(sent, w, 'v') for w in sent]
#     # synsets = [lesk(sent, w, 'v', lang = lang) for w in sent]
#     # synsets = [wn.synsets(ws, lang=lang) for ws in sent]
#     synsets = [lesk(sent, w, synsets = wn.synsets(w, lang = lang_code_3)) for w in sent]
    
#     # for ws in sent:
#     #     # for ss in [n for synset in  for n in synset.lemma_names(lang)]:
#     #     wn.synsets(ws, lang=lang)
    
#     synset_ID_list = []
            
#     print(synsets)
#     for synset in synsets:
        
#         if synset is None:
#             continue

#         # print(synset.lemma()[0])
#         # print(synset.lemma()[0].frame_ids())
#         print(synset.lemma_names(lang_code_3))
#         # print(synset.frame_ids())
#         # print('new')
#         synset_ID = wn.ss2of(synset)
#         print(synset_ID)
#         synset_ID_list.append(synset_ID)
#     e_synset_time = time.time()
#     print('Time synset {}'.format(e_synset_time - s_synset_time))

        
if __name__ == '__main__':
    main()