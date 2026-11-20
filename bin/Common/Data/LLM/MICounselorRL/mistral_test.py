import os
from mistralai.client import Mistral

MODEL = "open-mistral-nemo"

api_key = "WvFVga3uNDYHnKEj6IBr8jAvF0uMR7yu"

if not api_key:
    raise RuntimeError(
        "Missing MISTRAL_API_KEY. Set it first in your terminal."
    )

client = Mistral(api_key=api_key)

messages = [
    {
        "role": "system",
        "content": "You are a helpful assistant. Answer clearly and concisely."
    }
]

print("Mistral terminal chat")
print("Type 'exit' or 'quit' to stop.\n")

while True:
    user_input = input("You: ").strip()

    if user_input.lower() in {"exit", "quit"}:
        print("Bye!")
        break

    if not user_input:
        continue

    messages.append({"role": "user", "content": user_input})

    try:
        response = client.chat.complete(
            model=MODEL,
            messages=messages,
        )

        assistant_reply = response.choices[0].message.content
        print(f"\nMistral: {assistant_reply}\n")

        messages.append({
            "role": "assistant",
            "content": assistant_reply
        })

    except Exception as e:
        print(f"\nError: {e}\n")