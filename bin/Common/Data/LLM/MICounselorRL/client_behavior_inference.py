import argparse

from utils_mistral_online import *

intent_detail_list_en = read_prompt_csv('client')
intent_detail_list_fr = read_prompt_csv('client_fr')
# export API key to environment variable
def get_client_intent(utterance,context,language="EN"):
	utterance = utterance.strip()
	context = context.strip()
	if language=="EN":
		messages = create_message_client(intent_detail_list_en, utterance,context,language)
	else:
		messages = create_message_client(intent_detail_list_fr, utterance,context,language)
	response = get_completion_from_messages_local(messages, temperature=0.7)


	return response


