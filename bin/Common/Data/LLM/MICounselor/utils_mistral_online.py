import time
import os
import json
import csv
import pandas as pd
import openai
from tqdm import tqdm
import codecs
from mistralai.client import MistralClient
from mistralai.models.chat_completion import ChatMessage

api_key_file = os.path.join(os.path.dirname(__file__), 'api_key.txt')
with open(api_key_file, 'r') as f:
    MISTRAL_API_KEY = f.read()

model = "mistral-small-latest"

def read_prompt_csv(role):
  if role == 'therapist':

     filename = 'Common/Data/LLM/MICounselor/prompts/therapist_prompts.csv'
     #FR
     #filename = 'Common/Data/LLM/MICounselor/prompts/therapist_promptsFR.csv'

  if role == 'client':
      filename = 'Common/Data/LLM/MICounselor/prompts/client_prompts.csv'
      #FR
      #filename = 'Common/Data/LLM/MICounselor/prompts/client_promptsFR.csv'

  df = pd.read_csv(filename)
  intent_detail_list = []
  for index, row in df.iterrows():
      positive_examples = [row['positive example 1'],row['positive example 2'], row['positive example 3'], row['positive example 4'], row['positive example 5']]

      intent_detail_list.append({'intent': row['intent'].strip(),'definition': row['definition'],'positive_examples': positive_examples})
  return intent_detail_list

# def get_completion_from_messages_local(messages, temperature=0.7, max_tokens=50):
#     client = MistralClient(api_key=MISTRAL_API_KEY)
#
#
#     response = client.chat(
#         model=model,
#         messages=messages,
#         temperature=temperature,
#         max_tokens=max_tokens,
#     )
#
#     answer = response.choices[0].message.content
#     answer = answer.replace('\n', ' ')
#     answer = answer.replace('[', ' ')
#     answer = answer.replace(']', ' ')
#
#     return answer


def get_completion_from_messages_local(client,messages, temperature=0.7, max_tokens=50):


    # response = client.chat(
    #     model=model,
    #     messages=messages,
    #     temperature=temperature,
    #     max_tokens=max_tokens,
    # )

    response = client.chat(
        model=model,
        messages=messages,
        temperature=temperature,
        max_tokens=max_tokens,
    )

    answer = response.choices[0].message.content
    answer = answer.replace('\n', ' ')
    answer = answer.replace('[', ' ')
    answer = answer.replace(']', ' ')

    return answer

def create_message_client(intent_detail_list, utterance, context):

    intent_definition_list = []
    intent_example_list = []
    for intent_detail in intent_detail_list:
        intent_text = intent_detail['intent']
        definition_text = intent_detail['definition'].replace("\\", "")
        positive_example_list = intent_detail['positive_examples']


        for ex in positive_example_list:
            if len(ex)>3:
                intent_example_list.append(f"{ex}\n Category: {intent_text}")
        intent_definition_list.append(f' {intent_text}: {definition_text} ')
    intent_definition = ";\n".join(intent_definition_list)
    exemples = ";\n".join(intent_example_list)
    system_prompt_template = f""" You are a virtual therapist. Your task is to assess patient's intent and categorize patient's utterance after <<<>>> \
                                   into one of the following predefined categories:\n {intent_definition}\
                                     If the text doesn't fit into any of the above categories, classify it as: \n unknown\
                                     You will not invent new categories. \
                                     If multiple categories apply, list all of them.\
                                     You will only respond with the category. Do not provide explanations or notes.\
                                     ####
                                       Here are some examples:\n {exemples}
                                     ####

                                     <<<
                                       Context : {context}
                                       Patient's utterance: {utterance}
                                     >>>
                                     """
    ##FR
    # system_prompt_template = f""" Vous êtes un thérapeute virtuel. Votre tâche est d'évaluer l'intention du patient et de catégoriser l'énoncé du patient après <<<>>> \
    #                                dans l'une des catégories prédéfinies suivantes :\n {intent_definition}\
    #                                  Si le texte ne correspond à aucune des catégories ci-dessus, classez-le comme : \n inconnu\
    #                                  Vous n'inventerez pas de nouvelles catégories. \
    #                                  Si plusieurs catégories s'appliquent, listez-les toutes.\
    #                                  Vous ne répondrez qu'avec la catégorie. Ne fournissez pas d'explications ou de notes.\
    #                                  ####
    #                                    Voici quelques exemples :\n {exemples}
    #                                  ####
    #
    #                                  <<<
    #                                    Contexte : {context}
    #                                    Énoncé du patient : {utterance}
    #                                  >>>
    #                                  """

    messages = [ChatMessage(role= 'system', content= system_prompt_template)]
    return messages

def create_message_therapist(intent_detail_list, utterance, context):

    intent_definition_list = []
    intent_example_list = []
    for intent_detail in intent_detail_list:
        intent_text = intent_detail['intent']
        definition_text = intent_detail['definition'].replace("\\", "")
        positive_example_list = intent_detail['positive_examples']
        for ex in positive_example_list:
            try:
                if len(ex) > 3:
                    intent_example_list.append(f"{ex}\n Category: {intent_text}")
            except:
                pass
        intent_definition_list.append(f' {intent_text}: {definition_text} ')
        intent_definition = ";\n".join(intent_definition_list)
    exemples = ";\n".join(intent_example_list)
    system_prompt_template = f""" You are a virtual therapist. Your task is to assess therapist's intent and categorize therapist's utterance after the context \
                                    into one of the following predefined categories:\n {intent_definition}\
                                      Only if the text doesn't fit into any of the above categories, classify it as: \n unknown\
                                      You will not invent new categories. \
                                      A category can only apply to a portion of the text.\
                                      If multiple categories apply, list all of them.\
                                      You will only respond with the categories. Do not provide explanations or notes.\
                                       ####
                                       Here are some examples:\n {exemples}
                                     ####

                                     <<<
                                       Context : {context}
                                       Therapist's utterance: {utterance}
                                     >>>
                                     """
    # #FR
    # system_prompt_template = f""" Vous êtes un thérapeute virtuel. Votre tâche est d'évaluer l'intention du thérapeute et de catégoriser l'énoncé du thérapeute après le contexte \
    #                                 dans l'une des catégories prédéfinies suivantes :\n {intent_definition}\
    #                                   Seulement si le texte ne correspond à aucune des catégories ci-dessus, classez-le comme : \n inconnu\
    #                                   Vous n'inventerez pas de nouvelles catégories. \
    #                                   Une catégorie peut seulement s'appliquer à une portion du texte.\
    #                                   Si plusieurs catégories s'appliquent, listez-les toutes.\
    #                                   Vous ne répondrez qu'avec les catégories. Ne fournissez pas d'explications ou de notes.\
    #                                    ####
    #                                    Voici quelques exemples :\n {exemples}
    #                                  ####
    #
    #                                  <<<
    #                                    Contexte : {context}
    #                                    Énoncé du thérapeute : {utterance}
    #                                  >>>
    #                                  """


    messages = [ChatMessage(role= 'system', content= system_prompt_template)]

    return messages

