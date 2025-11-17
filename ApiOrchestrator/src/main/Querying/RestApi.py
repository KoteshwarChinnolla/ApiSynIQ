import requests
from urllib.parse import urlencode
from Processing.StringToPydantic import ReturnInputs, Inputs
from pydantic import BaseModel
import json
class RequestApi:
    def execute_input_request(self, inputs: Inputs):

        if not inputs or not inputs.example:
            raise ValueError("Inputs object or its 'input' section is missing.")

        inp = inputs.example
        print("inp", inp)
        # Build the URL
        base_url = inputs.globalPath.rstrip("/")
        endpoint = inputs.endpoint.lstrip("/")
        url = f"{base_url}/{endpoint}"


        # e.g., /user/{id}  →  /user/123
        if inp.inputPathParams:
            print("inp.inputPathParams", inp.inputPathParams)
            for key, value in inp.inputPathParams.items(): 
                url = url.replace(f"{{{key}}}", str(value))
        print("Final URL:", url)

        # Query Params
        query_params = inp.inputQueryParams or {}
        if query_params:
            url = f"{url}?{urlencode(query_params)}"

        # Headers / Cookies
        headers = inp.inputHeaders or {}
        cookies = inp.inputCookies or {}

        # Body Handling
        data = None
        json_data = None

        body = inp.inputBody
        if body is not None and body["requestBody"]:
            temp = json.loads(body["requestBody"])
            if isinstance(temp, dict):
                json_data = temp
                headers["Content-Type"] = "application/json"
            else:
                data = temp
            
        # Choose HTTP Method
        method = (inputs.httpMethod or "GET").upper()
        print("method", method)
        print("url", url)
        print("headers", headers)
        print("cookies", cookies)
        print("data", data)
        print("json_data", json_data)
        # Make the request
        response = requests.request(
            method=method,
            url=url,
            headers=headers,
            cookies=cookies,
            data=data,
            json=json_data
        )

        # Return JSON or fallback to text
        try:
            return {
                "status": response.status_code,
                "data": response.json()
            }
        except:
            return {
                "status": response.status_code,
                "data": response.text
            }