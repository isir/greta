"""
dialog_act_mapping.py

client_talk_type:          None=0, change=1, neutral=2, sustain=3
reflection_subtype:        None=0, simple=1, complex=2
question_subtype:          None=0, open=1, closed=2
main_therapist_behaviour:  None=0, reflection=1, question=2, therapist_input=3, other=4
therapist_input_subtype:   None=0, information=1, advice=2, negotiation=3, options=4
silences:                  [4] binary flags (NOT IMPLEMENTED YET) #ToDO

Note:
 - reflection_subtype (simple/complex) not distinguished
 when reflection, we assume simple.

"""

import numpy as np

MAP_CLIENT_TALK_TYPE = {'None': 0, 'change': 1, 'neutral': 2, 'sustain': 3}
MAP_REFLECTION_SUBTYPE = {'None': 0, 'simple': 1, 'complex': 2}
MAP_QUESTION_SUBTYPE = {'None': 0, 'open': 1, 'closed': 2}
MAP_MAIN_THERAPIST_BEHAVIOUR = {
    'None': 0, 'reflection': 1, 'question': 2, 'therapist_input': 3, 'other': 4
}
MAP_THERAPIST_INPUT_SUBTYPE = {
    'None': 0, 'information': 1, 'advice': 2, 'negotiation': 3, 'options': 4
}

THERAPIST_INTENT_TO_SCHEMA = {
    'Reflection':                              ('reflection', 'reflection_subtype', 'simple'),
    'Empathic Reaction':                       ('reflection', 'reflection_subtype', 'complex'),

    'Ask for Information':                      ('question', 'question_subtype', 'closed'),
    'Ask about current Emotions':              ('question', 'question_subtype', 'open'),
    'Invite to Shift Outlook':                 ('question', 'question_subtype', 'open'),
    'Ask for Consent or Validation':           ('question', 'question_subtype', 'closed'),

    'Give Solution':                           ('therapist_input', 'therapist_input_subtype', 'advice'),
    'Planning with the Patient':               ('therapist_input', 'therapist_input_subtype', 'negotiation'),
    'Medical Education and Guidance':          ('therapist_input', 'therapist_input_subtype', 'information'),
    'Experience Normalization and Reassurance':('therapist_input', 'therapist_input_subtype', 'information'),

    'Greeting or Closing':                     ('other', None, None),
    'Backchannel':                             ('other', None, None),
    'Progress Acknowledgment and Encouragement':('other', None, None),
    'unknown':                                 ('other', None, None),
}

CLIENT_INTENT_TO_TALK_TYPE = {
    'Changing unhealthy behavior in the future':   'change',
    'Sustaining unhealthy behavior in the future': 'sustain',
    
    'Sharing personal information or Describe past event': 'neutral',
    'Sharing negative feeling or emotion':         'neutral',
    'Sharing positive feeling or emotion':         'neutral',
    'Understanding or New Perspective':            'neutral',
    'Greeting or Closing':                         'neutral',
    'Backchannel':                                 'neutral',
    'Asking for Medical Information':              'neutral',
    'unknown':                                     'neutral',
}


def _clean_intent(raw):
    if raw is None:
        return 'unknown'
    text = str(raw).strip()
    
    for known in THERAPIST_INTENT_TO_SCHEMA:
        if known.lower() in text.lower():
            return known
    for known in CLIENT_INTENT_TO_TALK_TYPE:
        if known.lower() in text.lower():
            return known
    return 'unknown'


def therapist_intent_to_fields(raw_intent):
    intent = _clean_intent(raw_intent)
    main, subfield, subval = THERAPIST_INTENT_TO_SCHEMA.get(
        intent, ('other', None, None)
    )

    fields = {
        'main_therapist_behaviour': MAP_MAIN_THERAPIST_BEHAVIOUR[main],
        'reflection_subtype': 0,
        'question_subtype': 0,
        'therapist_input_subtype': 0,
    }

    if subfield == 'reflection_subtype':
        fields['reflection_subtype'] = MAP_REFLECTION_SUBTYPE[subval]
    elif subfield == 'question_subtype':
        fields['question_subtype'] = MAP_QUESTION_SUBTYPE[subval]
    elif subfield == 'therapist_input_subtype':
        fields['therapist_input_subtype'] = MAP_THERAPIST_INPUT_SUBTYPE[subval]

    return fields


def client_intent_to_field(raw_intent):
    intent = _clean_intent(raw_intent)
    talk_type = CLIENT_INTENT_TO_TALK_TYPE.get(intent, 'neutral')
    return {'client_talk_type': MAP_CLIENT_TALK_TYPE[talk_type]}


def empty_dialog_act():
    return {
        'client_talk_type': 0,
        'reflection_subtype': 0,
        'question_subtype': 0,
        'main_therapist_behaviour': 0,
        'therapist_input_subtype': 0,
        'silences': [0, 0, 0, 0],
    }

