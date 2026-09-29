# Definition of constants ##################################

## Nod blink
FPS = 30
EYES_CLOSED = 2.0
TIME_EYES_CLOSED_S = 0.4
TIME_EYES_CLOSED_MAX_S = 1.0
TIME_EYES_CLOSED_MAX_FRAMES = TIME_EYES_CLOSED_MAX_S*FPS

MERGE_GAP_FRAMES = int(0.15 * FPS)  # 150 ms

TIME_BEFORE = 0.4
TIME_AFTER = 0.4
TIME_LAST_SUCCESS = 1
TIME_NEXT_SUCCESS = 1

DURATION_CHUNK_S = 0.6
DURATION_CHUNK_FRAMES = int(DURATION_CHUNK_S * FPS)

DELAY_BETWEEN_TARGET_AND_OBSERVATION_S = 0.6
DELAY_BETWEEN_TARGET_AND_OBSERVATION_FRAMES = int(DELAY_BETWEEN_TARGET_AND_OBSERVATION_S * FPS)

#AI 
LATENT_DIM = 16

############################################################

## AUs

aus_speaking_1 = ['AU10_r','AU23_r','AU25_r','AU26_r']
aus_speaking_2 = ['AU14_r','AU17_r','AU15_r','AU07_r']
aus_eyebrows = ['AU01_r','AU02_r','AU04_r']
aus_eyes = ['AU05_r','AU06_r','AU07_r']
aus_rest = ['AU09_r','AU12_r','AU20_r','AU45_r']
aus_smile = ['AU12_r','AU14_r']

cols_au = ['AU01_r', 'AU02_r', 'AU04_r', 'AU05_r', 'AU06_r', 'AU07_r', 'AU09_r', 'AU10_r', 'AU12_r', 'AU14_r', 'AU15_r', 'AU17_r', 'AU20_r', 'AU23_r', 'AU25_r', 'AU26_r', 'AU45_r']

aus_to_increase = ['AU12_r', 'AU20_r', 'AU23_r', 'AU14_r', 'AU10_r', 'AU17_r', 'AU15_r', 'AU09_r']

'''
cols_to_save = ['transcript_id', 'timestamp', 'end', 'interlocutor',
       'client_talk_type', 'turn_id', 'sentence_id',
       'sentence_id_in_turn', 'therapist_input_subtype',
       'question_subtype', 'main_therapist_behaviour', 'AU01_r', 'AU02_r',
       'AU04_r', 'AU05_r', 'AU06_r', 'AU07_r', 'AU09_r', 'AU10_r',
       'AU12_r', 'AU14_r', 'AU15_r', 'AU17_r', 'AU20_r', 'AU23_r',
       'AU25_r', 'AU26_r', 'AU45_r']
'''

cols_to_save = ['transcript_id', 'timestamp', 'end', 'therapist_speaking',
       'client_speaking', 'client_talk_type', 'utterance_id','sentence_id',
       'turn_id','sentence_id_in_turn', 'therapist_input_subtype','reflection_subtype',
       'question_subtype', 'main_therapist_behaviour', 'silence_between_turn_therapist', 
       'silence_between_turn_client', 'id_silence_between_turn_therapist',
       'id_silence_between_turn_client','silence_within_turn_therapist',
       'silence_within_turn_client', 'id_silence_within_turn_therapist', 'id_silence_within_turn_client',
       'AU01_r', 'AU02_r', 'AU04_r', 'AU05_r', 'AU06_r', 'AU07_r', 
       'AU09_r', 'AU10_r','AU12_r', 'AU14_r', 'AU15_r', 'AU17_r',
       'AU20_r', 'AU23_r','AU25_r', 'AU26_r', 'AU45_r']

cols_dialog_acts_client = ['client_talk_type']

cols_dialog_acts_therapist = ['reflection_subtype','question_subtype', 
                    'main_therapist_behaviour', 'therapist_input_subtype',
                     'silence_between_turn_therapist', 'silence_between_turn_client',
                     'silence_within_turn_therapist', 'silence_within_turn_client']


dict_dialog_acts_client = {
    'client_talk_type': ['change','neutral','sustain']
}

dict_dialog_acts_therapist = {
    'reflection_subtype': ['simple','complex'],
    'question_subtype': ['open','closed'],
    'main_therapist_behaviour': ["reflection", "question", "therapist_input", "other"], # is it really necessary?
    'therapist_input_subtype': ["information", "advice", "negotiation", "options"],
    'silence_between_turn_therapist': [True,False],
    'silence_between_turn_client': [True,False],
    'silence_within_turn_therapist': [True,False],
    'silence_within_turn_client': [True,False]
}

map_silences = {
    'silence_between_turn_therapist': 0,
    'silence_between_turn_client': 1,
    'silence_within_turn_therapist': 2,
    'silence_within_turn_client': 3
}

map_reflection_subtype = {
    'None': 0,
    'simple': 1,
    'complex': 2
}
map_question_subtype = {
    'None': 0,
    'open': 1,
    'closed': 2
}
map_main_therapist_behaviour = {
    'None': 0,
    "reflection": 1,
    "question": 2,
    "therapist_input": 3,
    "other": 4
}
map_therapist_input_subtype = {
    'None': 0,
    "information": 1,
    "advice": 2,
    "negotiation": 3,
    "options": 4
}
map_client_talk_type = {
    'None': 0,
    'change': 1,
    'neutral': 2,
    'sustain': 3
}


############################################################

# Definition of functions ##################################

import datetime

def convert(n,fps = FPS):
    return str(datetime.timedelta(seconds = n/fps))

def minutes_to_frames(minutes = 0 ,seconds = 0 ,fps = FPS):
    return (60*minutes + seconds)*fps

def convert_back(time_str, fps = FPS):
    h, m, s = map(float, time_str.split(':'))
    total_seconds = h * 3600 + m * 60 + s
    return int(total_seconds * fps)