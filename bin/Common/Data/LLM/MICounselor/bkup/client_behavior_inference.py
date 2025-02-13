import argparse

from utils_mistral_online import *

intent_detail_list = read_prompt_csv('client')
# export API key to environment variable
def get_client_intent(utterance,context):
	utterance = utterance.strip()
	context = context.strip()
	messages = create_message_client(intent_detail_list, utterance,context)
	response = get_completion_from_messages_local(messages, temperature=0.7)


	return response


