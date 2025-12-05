from typing_extensions import TypedDict,List,Dict,Any,Type 
from langgraph.graph import StateGraph, START, END
from langgraph.checkpoint.memory import InMemorySaver
import json, re
from langchain_google_genai import ChatGoogleGenerativeAI
from pydantic import BaseModel, Field
from Processing.StringToPydantic import GeneratePydantic
from Retrieval.data_pb2 import query
from langchain.agents import create_agent 
from pydantic import BaseModel, Field
from Querying.RestApi import RequestApi
from typing import Union
import json, re
from dataclasses import dataclass
from langchain.tools import tool, ToolRuntime 
# from Generation.llm_tools import speak
from Transcribe.TextToSpeech import SpeakTranscribe
from Generation.prompts import (
    human_feedback_prompt,
    normalize_feedback_prompt,
    modify_data_prompt,
    update_json_prompt
)

from dotenv import load_dotenv
import os
 

GEMINI_API_KEY = os.getenv("GEMINI_API_KEY")

@dataclass
class Context:
    context_data:List

# ----- STATE DEFINITION -----
class State(TypedDict):
    input: str
    user_feedback: str
    employee_data: dict
    employee_summary: str
    decision: str
    selected_model: str
    pydantic_schemas:dict
    selected_markdown:str
    output:str
    selected_apiname:str
    # selected_inputs:Any

# ----- CLASS-BASED LANGGRAPH WORKFLOW -----
class GraphWorkflow:
    def __init__(self):
        self.llm_model=ChatGoogleGenerativeAI(
        # model="gemini-2.0-flash",api_key="AIzaSyD5DDGMH3Tl-z7JR-qZZaLEN1CtjDx0MuU"
        model="gemini-2.0-flash",api_key=GEMINI_API_KEY
        # model="gemini-2.0-flash",api_key="AIzaSyAO6OXuED60hgJLgmupmIepHFsWYbU8in8"
        # model="gemini-2.0-flash",api_key="AIzaSyBDXlRvI7hwDbR4N_biI02tz80UCK0QuF4"

        )
        self.memory = InMemorySaver()
        self.graph = self._build_graph()
        self.schema_classes = {}
        self.object_store={}
        self.context=Context(
            context_data=[]
        )
        

   
    def model_selector(self,state: State):
        print("\n--- Step 1: Automatic API Selection ---")
        user_input=state["input"]
        gen=GeneratePydantic()
        user_input = state.get("input", "")
        a = query(query=user_input, limit=3)
        fetched_results=gen.Fetch(a,"RETURN")
        print(fetched_results.items())
        if not fetched_results:
            print("⚠️ No API matches found!")
            return {"selected_model": None}
        api_names = list(fetched_results.keys())
        inputs_=list(fetched_results.values())
        (api1,api2,api3),(inp1,inp2,inp3) =api_names,inputs_
        markdown1 = getattr(inp1, "markDown", "")
        markdown2 = getattr(inp2, "markDown", "")
        markdown3 = getattr(inp3, "markDown", "")
        selection_prompt = f"""
        You are an intelligent API selection system.
        User Query:
        {user_input}There are 3 API candidates below. Choose the BEST one that matches the user's intent.
        API 1: {api1} Description: {markdown1}
        API 2: {api2} Description: {markdown2}
        API 3: {api3} Description: {markdown3}
        RULES:Read the every API description and api carefully and choose the one that best fits the user's request.
        Respond with EXACTLY one of: 1, 2, or 3.Do NOT explain your answer.
        Just output the best matching API number.give me the best that suits the user query.
        """
        llm_choice = self.llm_model.invoke(selection_prompt).content.strip()

        if llm_choice == "1":
            selected_api = api1
            selected_inputs = inp1
            selected_markdown=markdown1

        elif llm_choice == "2":
            selected_api = api2
            selected_inputs = inp2
            selected_markdown=markdown2

        else:
            selected_api = api3
            selected_inputs = inp3
            selected_markdown=markdown3

        print(f"\n✅ Selected API: {selected_api}")
        selected_inputbody = selected_inputs.input.inputBody
        selected_pathparams = selected_inputs.input.inputPathParams
        selected_queryparams = selected_inputs.input.inputQueryParams
        selected_variables=selected_inputs.input.inputVariables
        selected_headers=selected_inputs.input.inputHeaders
        selected_cookies=selected_inputs.input.inputCookies

        self.schema_classes[selected_api] = {
            "inputBody":selected_inputbody,
            "inputPathParams": selected_pathparams,
            "inputQueryParams":selected_queryparams,
            "inputVariables":selected_variables,
            "inputHeaders":selected_headers,
            "inputCookies":selected_cookies
        }
        self.object_store[selected_api]=selected_inputs
        return {
        "selected_apiname": selected_api,
        "selected_markdown":selected_markdown,
        "pydantic_schemas": {
            "inputBody": selected_inputbody.model_json_schema() if selected_inputbody else None,
                "inputPathParams": selected_pathparams.model_json_schema() if selected_pathparams else None,
                "inputQueryParams": selected_queryparams.model_json_schema() if selected_queryparams else None,
                "inputVariables":selected_variables.model_json_schema() if selected_variables else None,
                "inputHeaders":selected_headers.model_json_schema() if selected_headers else None,
                "inputCookies":selected_cookies.model_json_schema() if selected_cookies else None
            },
        }



    def llm(self, state: State):
        print(f"\n🚀 Agent-based LLM Node for: {state['selected_apiname']}")
        schemas = state["pydantic_schemas"]
        apiname = state["selected_apiname"]
        markdown = state["selected_markdown"]
        active_schemas = {}

        for k in ["inputBody", "inputPathParams", "inputQueryParams","inputVariables","inputHeaders","inputCookies"]:
            schemas.get(k)
            active_schemas[k] = self.schema_classes[apiname][k]
        print(active_schemas)
        schema_union_list = [s for s in active_schemas.values() if s]
        print(schema_union_list)
        filled_data = {}

        # Combine schemas for structured output
        for i in active_schemas:
            if active_schemas[i] is None:
                filled_data[i] = None
            else:
                prompt = f"""Read the markdown description {markdown} carefully and collect 
                all the necessary fields  which are in pydatic object {active_schemas[i]} from the user in a human-like manner.and use the given
                pydantic object to read the descritpion in a natural way
                Now your task is to generate a question which fields exists only in the given pydantic object and ignore the context fields already filled fields to ask the user to collect all the necessary
                fields from the pydantic object except the pydantic feilds which are already filled in the context {self.context.context_data}in a single question in which agent can gather all the pydantic feilds
                to the pydantic object by collecting information from the user in 2-3 lines strictly.just give me 2-3 lines of question of what user needs to answer thats it.
                Dont give me the irrelavant data just straight forwardly ask the question
                """
                result = active_schemas[i]()  # initialize empty pydantic object
                question = self.llm_model.invoke(prompt).content.strip()

                while True:
                    # Ask user input

                    user_input = SpeakTranscribe.tts_worker(question)
                    # Agent prompt
                    system_prompt = f"""
                    Your goal:
                        You are a friendly assistant collecting structured data from the user {user_input}.
                    Ask all missing fields together from the user input and map them into the given Pydantic model {active_schemas[i]}. Be natural and conversational based
                    on the question '{question}' without changing it format store the data in pydantic object and given pydantic object from the llm.
                    You are a precise structured-data extraction agent.
                    - Preserve already-collected fields {result.model_dump()} and store only values from the user which are null values in it. Never delete or overwrite previously confirmed data.
                    - Ask all missing fields together in one friendly message.
                    - Never hallucinate, guess, invent, or assume values.
                    - Never fill fields with null, unknown, N/A, or placeholders.
                    - Continue the conversation until **every field** in the Pydantic model is fully collected from the user.
                    - Once all values are present, return structured JSON exactly matching the schema.
                    if user provided the date then take it and pass into the pydantic object and dont throw an error

                    Rules:
                    1. Extract strictly from user input.
                    2. Keep previously filled fields as-is.
                    3. Ask only for missing fields.
                    4. Do not stop until model is complete.
                    5. When asking for missing fields, be polite, clear, and use normal conversational language.
                    6.  dont make any tool calling errors, just take what ever the user gives and convert according to pydantic fields.
                    """

                    agent = create_agent(
                        model=self.llm_model,
                        system_prompt=system_prompt,
                        response_format=active_schemas[i]  # DIRECT pydantic class
                    )

                    response = agent.invoke({
                        "messages": [
                            {"role": "user", "content": user_input},
                            {"role": "assistant", "content": result.model_dump_json()}
                        ]
                    })

                    # Merge the newly extracted data into result
                    new_data = response["structured_response"].model_dump()
                    for k, v in new_data.items():
                        if v not in (None, ""):
                            setattr(result, k, v)

                    # Check if any fields still missing
                    missing_fields = [
                        f for f in active_schemas[i].model_fields
                        if getattr(result, f) in (None, "")
                    ]
                    if not missing_fields:
                        # All fields filled
                        filled = result.model_dump_json(indent=4)
                        summary=f"""
                        give the current '{apiname}' to the users in way that you have entered these data in one or two natural sentences.
                        Data:and talk to the user that i collected information from you is 
                        {json.dumps(result.model_dump(), indent=2)
                        } give simple and effective answers in just two-three lines
                        Output only the summary sentence(s). just in 2-3 lines of length in which it can covers all the information
                        """
                        collected_info=self.llm_model.invoke(summary).content.strip()
                        print("LLM Response :",collected_info)
                        print(filled)
                        if i=="inputBody":
                            a={"requestBody":result.model_dump_json(indent=4)}
                            filled_data[i]={"requestBody":result.model_dump_json(indent=4)}
                            self.context.context_data.append(response['structured_response'])
                        else:
                            filled_data[i]=result.model_dump_json(indent=4)
                            self.context.context_data.append(response['structured_response'])
                        print(self.context.context_data)
                        break
                    else:
                        # Generate a friendly question for missing fields
                        missingfields_question = f"""
                        You are a friendly AI assistant helping to fill a Pydantic model {active_schemas[i]}.
                        Fields still missing and filled with null: {result.model_dump_json(indent=4)}.and ignore the pydantic 
                        feilds which are already stored in the context {self.context.context_data}.DON'T collect feilds which are in given context
                        Generate ONE polite, human-like question asking only for these missing fields which have null values.
                        Do NOT ask about fields already filled.In question clearly mention all the missing feilds to be collect 
                        in order user can understand clearly to answer them.Output only the question text.
                        """                                            
                        question = self.llm_model.invoke(missingfields_question).content.strip()
        print(filled_data)
        return {"employee_data": filled_data, "selected_model": apiname}

   
    # ---------------- NODE 3: HUMAN FEEDBACK ----------------
    def human_assistance(self, state: State):
        # print("\n--- Step 2: Human Feedback ---")
        selected_model = state["selected_apiname"]
        model_data = state.get("employee_data", {})
        if not model_data:
            print("⚠️ No data found for feedback.")
            return {"user_feedback": "no_data"}

        approval_prompt = f"""
        You are a friendly assistant showing collected employee details.
        Here is the information:
        {json.dumps(model_data, indent=2)}
        Ask the user if everything looks correct, but in a human way.
        Avoid robotic phrases like "approve" or "retry".
        Example:
        "Looks great! Would you like me to make any changes or is everything good to go?" or to change any data or modify your
        Output only the conversational question. just in 2-3 lines
        """
        # approval_question = self.llm_model.invoke(approval_prompt).content.strip()
        approval_question = self.llm_model.invoke(human_feedback_prompt(model_data)).content.strip()
        
        # feedback=speak(approval_question)
        feedback=""
        # while not feedback:
        #     feedback = speak("Please provide valid feedback: ")

        normalize_prompt = f"""
        You are a classifier that decides if the user's feedback means 'accept' or 'modify'.
        Classify as:
        "accept" → if the user says things like "yes", "it's good","pretty", "looks fine", "okay", "all correct", "no changes", "done",any greetings or anything meaning approval,which are postive responses from user
        "modify" → if the user says things like "change", "edit", "update", "not correct", or anything meaning modification which are negetive responses from user
        User feedback: "{feedback}"
        Output only one word: accept or modify.
        """
        # normalized = self.llm_model.invoke(normalize_prompt).content.strip().lower()
        normalized = self.llm_model.invoke(normalize_feedback_prompt(feedback)).content.strip().lower()

        print(f"\n🗣️ Normalized Feedback: {normalized}")

        return {"user_feedback": normalized, "decision": normalized, "selected_model": state["selected_apiname"]}

    # ---------------- NODE 4: PROCESS FEEDBACK ----------------
    def process_feedback(self, state: State):
        # print(f"\n--- Step 3: Processing Feedback for {state['selected_model']} ---")
        decision = state.get("decision", "")
        model_data = state.get("employee_data", {})
        selected_model = state["selected_apiname"]
        inputs=self.object_store[selected_model]

        if not model_data:
            return {"output": "no_data", "decision": "accept"}
        
        if decision == "accept":
            for k,v in model_data.items():
                if v is not None:
                    setattr(inputs.example,k,model_data[k])
            print("output:",inputs.example)
            obj = RequestApi()
            response = obj.execute_input_request(inputs)
            print("Backend Response:", response)

            a=RequestApi()
            res=a.execute_input_request(inputs)
            # print(res)
            summary=f"""
                    give the response for the user collected data by taking user data detials and give me the content like that you are requested date is 
                    {res} give simple and effective answers in just two-three lines without missing any feild data 
                    Output only the summary sentence(s). just in 2-3 lines of length in which it can covers all the information
                    """
            print("LLM Response :",self.llm_model.invoke(summary).content.strip().lower())
            return {"output":"Successfully completed"}

        if state["decision"] == "modify":
            # print("\n🔄 User requested modifications.")
            modify_prompt = f"""
            You’re assisting a user in modifying a '{selected_model}' record.
            Ask politely what they want to update (date, name, status, etc.).
            Output only the question. just in 2-3 lines
            """
            # modify_question = self.llm_model.invoke(modify_prompt).content.strip()
            modify_question = self.llm_model.invoke(modify_data_prompt(selected_model)).content.strip()
            
            # print(f"\n🤖 LLM Question: {modify_question}")
            user_modification = input(f"{modify_question}\n\n🧍 Your Response: ")
            update_prompt = f"""
            You are a JSON editor for '{selected_model}'.Current JSON:
            {json.dumps(model_data, indent=2)} User said: "{user_modification}"Update the JSON accurately.
            Return ONLY the corrected JSON object.
            """

            updated_response = self.llm_model.invoke(update_json_prompt(selected_model, model_data, user_modification)).content.strip()
            json_match = re.search(r"\{.*\}", updated_response, re.DOTALL)
            if not json_match:
                print("⚠️ Could not extract JSON.")
                return {"output": "update failed.", "decision": "modify"}

            updated_data = json.loads(json_match.group(0))
            print("\n✅ Updated Data:")
            print(json.dumps(updated_data, indent=2))

            return {"employee_data": updated_data, "selected_model": selected_model, "decision": "modify"}

        else:
            return {"output": "Invalid feedback.", "decision": "accept"}

    # ---------------- BUILD LANGGRAPH ----------------
    def _build_graph(self):
        builder = StateGraph(State)
        builder.add_node("model_selector", self.model_selector)
        builder.add_node("llm", self.llm)
        builder.add_node("human_assistance", self.human_assistance)
        builder.add_node("process_feedback", self.process_feedback)

        builder.add_edge(START, "model_selector")
        builder.add_edge("model_selector", "llm")
        builder.add_edge("llm", "human_assistance")
        builder.add_edge("human_assistance", "process_feedback")
        builder.add_conditional_edges(
            "process_feedback",
            lambda state: state["decision"],
            {"accept": END, "modify": "human_assistance"},
        )

        return builder.compile(checkpointer=self.memory)
    def run(self, user_input: str):
        print("\n🚀 Starting LangGraph Workflow...\n")
        result = self.graph.invoke(
        {"input": user_input},
        config={"configurable": {"thread_id": "run_1"}}
        )
        # print(f"\n✅ LangGraph Execution Complete! \n{json.dumps(result, indent=2)}")
        return result 

# # ----- MAIN ENTRY POINT -----
# if __name__ == "__main__":
#     # llm_model = ChatGroq(
#     #     temperature=0.1,
#     #     groq_api_key="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",
#     #     model_name="llama-3.1-8b-instant" 
    
#     # )
#     # llm_model= ChatOllama(
#     #         model="qwen2",   
#     #         temperature=0.7
#     # )

    # workflow = GraphWorkflow()
    # user_input = input("💬 Enter initial prompt: ")
    # workflow.run(user_input)