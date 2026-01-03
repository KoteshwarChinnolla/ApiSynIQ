from typing import Any, Dict, Optional, Union
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
            # print("inp.inputPathParams", inp.inputPathParams)
            temp=json.loads(inp.inputPathParams)
            print(temp)
            for key, value in temp.items(): 
                url = url.replace(f"{{{key}}}", str(value))
        print("Final URL:", url)

        # Query Params
        query_params = json.loads(str(inp.inputQueryParams)) or {}

        if query_params:
            # print("inp.inputQueryParams", query_params)
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
    def _parse_maybe_json(self,value: Optional[Union[str, dict]]) -> Optional[Union[dict, list, str]]:
        """
        Try to parse a value that may be:
        - None
        - a JSON string representing an object/array
        - already a dict/list or plain string
        Returns parsed Python object or the original value if parsing fails.
        """
        if value is None:
            return None

        if isinstance(value, (dict, list)):
            return value

        if isinstance(value, str) and value.strip().lower() == "null":
            return None

        if isinstance(value, str):
            try:
                return json.loads(value)
            except json.JSONDecodeError:
                return value
        return value
    
    def query(self,
        inp: dict,
        method: str,
        url: str,
        *,
        timeout: float = 10.0,
        allow_raise_for_status: bool = False
    ) -> Dict[str, Any]:
        """
        Build and execute an HTTP request from a structured 'inp' dict.
        """

        print("inp received", inp)
        if not isinstance(inp, dict):
            raise ValueError("`inp` must be a dict describing the request inputs.")

        path_obj = self._parse_maybe_json(inp.get("path"))
        query_obj = self._parse_maybe_json(inp.get("query")) or {}
        headers_obj = self._parse_maybe_json(inp.get("headers")) or {}
        cookies_obj = self._parse_maybe_json(inp.get("cookies")) or {}
        body_obj = self._parse_maybe_json(inp.get("body"))

        
        final_url = url
        if isinstance(path_obj, dict):
            for key, val in path_obj.items():
                final_url = final_url.replace("{" + str(key) + "}", str(val))

        if isinstance(query_obj, dict) and query_obj:
            final_url = f"{final_url}?{urlencode(query_obj)}"


        if not isinstance(headers_obj, dict):
            raise ValueError("`headers` must be a dict or JSON-encoded dict.")
        

        if not isinstance(cookies_obj, dict):
            raise ValueError("`cookies` must be a dict or JSON-encoded dict.")

        json_payload = None
        data_payload = None
        if body_obj is not None:
            if isinstance(body_obj, (dict, list)):
                json_payload = body_obj
                if not any(k.lower() == "content-type" for k in headers_obj):
                    headers_obj["Content-Type"] = "application/json"
            else:
                data_payload = body_obj

        print("final_url", final_url)
        with requests.Session() as session:
            try:
                resp = session.request(
                    method=method,
                    url=final_url,
                    headers=headers_obj,
                    cookies=cookies_obj,
                    json=json_payload,
                    data=data_payload,
                    timeout=timeout,
                )
            except requests.RequestException as exc:
                return {
                    "status": None,
                    "error": str(exc),
                    "final_url": final_url,
                }

        # Optionally raise for status
        if allow_raise_for_status:
            try:
                resp.raise_for_status()
            except requests.HTTPError as exc:
                return {
                    "status": resp.status_code,
                    "error": str(exc),
                    "text": resp.text,
                    "headers": dict(resp.headers),
                    "final_url": final_url,
                }

        # Try JSON decode, else fallback to text
        parsed_data = None
        try:
            parsed_data = resp.json()
        except (ValueError, json.JSONDecodeError):
            parsed_data = resp.text

        result = {
            "status": resp.status_code,
            "data": parsed_data,
            "headers": dict(resp.headers),
            "cookies": resp.cookies.get_dict(),
            "elapsed_ms": int(resp.elapsed.total_seconds() * 1000),
            "final_url": resp.url,
        }
        return result    
    
    def execute_output_request_dict( inp: dict, method: str, url: str) -> dict:

        if type(inp) != dict:
            raise ValueError("Inputs object or its 'input' section is missing.")


        if "path" in inp and inp["path"] is not None and inp["path"] != "":
            temp=json.loads(inp["path"])
            for key, value in temp.items(): 
                url = url.replace(f"{{{key}}}", str(value))

        if "query" in inp and inp["query"] is not None and inp["query"] != "":
            query_params = json.loads(str(inp["query"])) or {}

            if query_params:
                url = f"{url}?{urlencode(query_params)}"

        # Headers / Cookies
        if "headers" in inp and inp["headers"] is not None and inp["headers"] != "":
            headers = inp["headers"]
        if "cookies" in inp and inp["cookies"] is not None and inp["cookies"] != "":
            cookies = inp["cookies"]

        # Body Handling
        data = None
        json_data = None

        
        if "body" in inp and inp["body"] is not None and inp["body"] != "":
            body = inp["body"]
            temp = json.loads(body)
            if isinstance(temp, dict):
                json_data = temp
                headers["Content-Type"] = "application/json"
            else:
                data = temp
            
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