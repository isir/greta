import argparse

from utils_mistral_online import *

intent_detail_list = read_prompt_csv('therapist')
def get_therapist_intent(client_online,utterance,context):
	utterance = str(utterance).strip()
	context = str(context).strip()
	messages = create_message_therapist(intent_detail_list, utterance,context)

	response = get_completion_from_messages_local(client_online,messages, temperature=0.7, max_tokens = 15)
	#response = get_completion_from_messages_local(messages, temperature=0.7)
	#print("[DA] " + response)

	return response

