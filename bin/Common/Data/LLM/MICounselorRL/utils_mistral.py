import time
import os
import json
import csv
import pandas as pd
import openai
from tqdm import tqdm
import codecs
from openai import OpenAI, AsyncOpenAI

def read_prompt_csv(role):
  if role == 'therapist':
     filename = 'prompts/therapist_prompts.csv'
  if role == 'client':
      filename = 'prompts/client_prompts.csv'
  df = pd.read_csv(filename)
  intent_detail_list = []
  for index, row in df.iterrows():
      print(row)
      positive_examples = [row['positive example 1'],row['positive example 2'], row['positive example 3'], row['positive example 4'], row['positive example 5']]

      intent_detail_list.append({'intent': row['intent'].strip(),'definition': row['definition'],'positive_examples': positive_examples})
  return intent_detail_list

def get_completion_from_messages_local(messages, temperature=0.7):
    client = OpenAI(base_url="http://localhost:1234/v1", api_key="not-needed")


    response = client.chat.completions.create(
                                            model="local-model",
                                            messages=messages,
                                            temperature=temperature,
                                        )

    return response.choices[0].message.content

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
    messages = [{'role': 'system', 'content': system_prompt_template}]
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

    messages = [{'role': 'system', 'content': system_prompt_template}]

    return messages

