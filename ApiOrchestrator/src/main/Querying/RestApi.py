import requests
from urllib.parse import urlencode
from Processing.StringToPydantic import ReturnInputs, Inputs

class RequestApi:
    def execute_input_request(self, inputs: Inputs):


        if not inputs or not inputs.input:
            raise ValueError("Inputs object or its 'input' section is missing.")

        inp = inputs.input

        # Build the URL
        base_url = inputs.globalPath.rstrip("/")
        endpoint = inputs.endpoint.lstrip("/")
        url = f"{base_url}/{endpoint}"


        # e.g., /user/{id}  →  /user/123
        if inp.inputPathParams and isinstance(inp.inputPathParams, dict):
            for key, value in inp.inputPathParams.items():
                url = url.replace(f"{{{key}}}", str(value))

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
        if body is not None:
            if isinstance(body, (dict, list)):
                json_data = body # JSON body
            else:
                data = str(body) # Strings

        # Choose HTTP Method
        method = (inputs.httpMethod or "GET").upper()

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