# Update Leave
**Method:** `PUT`  **Endpoint:** `leaves/{id}`
**Auto Execute:** `True`
---
## ⚙️ Inputs
**Description:** Allows an employee or manager to modify an existing leave request, such as changing the date or updating the status.

### 🔸 Path Parameters

id: 123456

### 🔸 Request Body
```json
{
    "serialVersionUID": 123456,
    "Action": "String",
    "Details": "String",
    "Leave_on": "String",
    "Rejection_Reason": "String",
    "isHalf_Day": true,
    "employeeId": 123456,
    "id": 123456,
    "Leave_type": "String",
    "Action_Date": "String",
    "status": "String",
    "Request_By": "String"
}
```
---

## 📦 Response
**Description:** Returns a message confirming that the leave details were updated successfully.

### 🔸 Response Body
```json
"String"
```
inputPathParams <class 'inputPathParams'>

=== CLASS INFO ===
Name: inputPathParams
Module: builtins
Bases: (<class 'pydantic.main.BaseModel'>,)

=== FIELDS ===
Field: id
  Type: typing.Optional[str]
  Default: None
  Description: Leave ID to update.
  Example: None

=== ANNOTATIONS ===
{'id': typing.Optional[str]}

=== JSON SCHEMA ===
{'properties': {'id': {'anyOf': [{'type': 'string'}, {'type': 'null'}], 'default': None, 'description': 'Leave ID to update.', 'title': 'Id'}}, 'title': 'inputPathParams', 'type': 'object'}
inputBody None
inputHeaders None
inputCookies None
inputQueryParams None
inputVariables None
# Apply Leave
**Method:** `POST`  **Endpoint:** `leaves/apply`
**Auto Execute:** `True`
---
## ⚙️ Inputs
**Description:** Endpoint to submit a new leave request. Employees can apply for paid, unpaid, casual, or sick leave with appropriate details.

### 🔸 Query Parameters

employeeId: 123456

### 🔸 Request Body
```json
{
    "serialVersionUID": 123456,
    "Action": "String",
    "Details": "String",
    "Leave_on": "String",
    "Rejection_Reason": "String",
    "isHalf_Day": true,
    "employeeId": 123456,
    "id": 123456,
    "Leave_type": "String",
    "Action_Date": "String",
    "status": "String",
    "Request_By": "String"
}
```
---

## 📦 Response
**Description:** Returns a success message confirming the leave application submission.

### 🔸 Response Body
```json
"String"
```
inputPathParams None
inputBody None
inputHeaders None
inputCookies None
inputQueryParams <class 'inputQueryParams'>

=== CLASS INFO ===
Name: inputQueryParams
Module: builtins
Bases: (<class 'pydantic.main.BaseModel'>,)

=== FIELDS ===
Field: employeeId
  Type: typing.Optional[str]
  Default: None
  Description: Employee ID for whom the leave is applied.
  Example: None

=== ANNOTATIONS ===
{'employeeId': typing.Optional[str]}

=== JSON SCHEMA ===
{'properties': {'employeeId': {'anyOf': [{'type': 'string'}, {'type': 'null'}], 'default': None, 'description': 'Employee ID for whom the leave is applied.', 'title': 'Employeeid'}}, 'title': 'inputQueryParams', 'type': 'object'}
inputVariables None

# YOU : 
Hey there can give me a detailed explanation of the tools you use and why, rule to be followed before using tools

# AI :

 Sure! Here’s a clear rundown of the tools at my disposal, what each one does, and the rules I follow before using them.

---

## 1. **`get_apis`**
**What it does:**  
Searches a catalog of available APIs and returns the definitions that match a short, precise description of the task you need performed.

**When I use it:**  
- Only when solving your request *requires* an external API (e.g., creating an order, looking up a user, sending an email).  
- I never call it for pure knowledge, reasoning, or conceptual questions.

**How I call it:**  
- I craft a concise query that describes the data I need to **send** (type `INPUT`) or **retrieve** (type `RETURN`).  
- I usually ask for just one API (`count = 1`) unless the problem truly needs several.

---

## 2. **`api_resolver`**
**What it does:**  
Acts as a sub‑agent that actually invokes the selected API. I give it:
- The exact API name (as returned by `get_apis`).  
- A short, factual description of why the call is needed (just for my own tracking; it doesn’t affect the call).

**When I use it:**  
- After I have a relevant API from `get_apis`.  
- Only if the task cannot be completed by reasoning alone.

**What I do with the response:**  
- I interpret the result and present it to you in plain language.  
- I update any tracking file (see below) to mark the call as completed.

---

## 3. **File‑based “Todo” tools** (`write_file`, `read_file`, `edit_file`)
These three tools let me keep a persistent, ordered plan when a request needs **multiple** API calls.

| Tool | Purpose | When it’s used |
|------|---------|----------------|
| **`write_file`** | Creates a new TODO list file (`/todos/.txt`). Each line follows the format `API_NAME | EXECUTION_REASON | EXECUTION_STATUS | API_RESPONSE`. | **Only** for multi‑step tasks that need more than one API call. I write the plan **once** per session. |
| **`read_file`** | Reads the current TODO list (must always be read before any edit). | Whenever I need to inspect the plan—e.g., before updating a status or adding a new step. |
| **`edit_file`** | Updates the TODO list (e.g., change a status from `planned` → `in_progress` → `completed` or add a new row if the user’s needs change). | After each API call, or if something goes wrong and the plan must be adjusted. I never rewrite the whole file—only the minimal necessary change. |

### Rules for the Todo files
1. **Never** create a TODO file for a single‑step task. Simple requests go straight to `api_resolver`.  
2. **Always** read the file first (`read_file`) before any edit.  
3. Only the fields `EXECUTION_STATUS` and `API_RESPONSE` may be changed after the file is created.  
4. Status transitions must be:
   - `planned → in_progress`
   - `in_progress → completed` **or** `in_progress → failed`
5. I must keep the exact formatting (`|`‑separated columns) and the order of rows unless the user changes the requirements.

---

## 4. **Decision Flow I Follow**

1. **Classification:**  
   - I first check if the question can be answered with pure reasoning/knowledge. If yes, I answer directly—no tools.  

2. **API Discovery:**  
   - If an external API is needed, I call `get_apis` **once** with a tight query.  

3. **Planning:**  
   - If more than one API call is required, I create a TODO list with `write_file`.  
   - For a single API call, I skip the TODO file and go straight to `api_resolver`.  

4. **Execution Loop:**  
   - For each TODO item, I invoke `api_resolver`, show you the result, and update the file with `edit_file` as needed.  

5. **Tool Minimization:**  
   - I always aim to use the fewest possible tool calls, API calls, and tokens while still fulfilling the request.

---

### Why these rules exist
- **Predictability & Traceability:** The TODO file gives a clear, auditable trail of what’s been planned, what’s in progress, and what’s finished.  
- **Safety:** By reading the file before editing, I avoid accidental overwrites or lost information.  
- **Efficiency:** Limiting calls to `get_apis` and the file‑write step means less latency and fewer chances for error.  
- **Clarity for you:** You can see exactly which APIs are being used and why, and you’ll get a concise explanation of each response.

---

If you have any specific scenario you’d like to see this process in action, just let me know!

# YOU : 
can i know how subagent will be used and invoked

# AI :
 ### How the **sub‑agent** (`api_resolver`) works and how it’s invoked

| Step | What happens | Who does it | What you see |
|------|--------------|-------------|--------------|
| **1. Identify the needed API** | I first figure out which external API can satisfy your request (e.g., “create an order”, “fetch a user profile”). | **Me (the primary assistant)** | I may run **`get_apis`** once to fetch the exact API definition. |
| **2. Build a tiny “task” description** | I write a short sentence that explains *why* I’m calling the API. This is only for bookkeeping; it does **not** affect the API call itself. | **Me** | Example: `"Create a new purchase order for customer 12345"` |
| **3. Call the sub‑agent** | I invoke the **`api_resolver`** tool, passing two arguments: 
• `selected_api` – the exact API name returned by `get_apis` (must match the key 100 %); 
• `task` – the short explanation from step 2. | **`api_resolver` sub‑agent** | The sub‑agent asks you (or uses its internal UI) for any required parameters, then sends the request to the real API endpoint. |
| **4. Receive the API response** | The sub‑agent returns the raw response (usually a JSON object) back to me. | **`api_resolver`** | Example response: `{"order_id":"ORD‑9876","status":"created"}` |
| **5. Translate into human‑readable output** | I take that response, interpret any codes, and formulate a clear answer for you. | **Me** | “Your order was created successfully. The order ID is **ORD‑9876**.” |
| **6. Update the TODO (if we’re in a multi‑step flow)** | If the overall request needs several API calls, I open the session’s todo file, change the row for this API from `in_progress` → `completed`, and paste the response into the `API_RESPONSE` column. | **Me** (using `edit_file`) | The file now reflects the finished step. |

#### Key points about the invocation

1. **Exact API name** – The `selected_api` argument must be **exactly** the key returned by `get_apis`. No renaming, no partial matches.
2. **One‑time call per step** – For a single‑step request I call `api_resolver` directly after `get_apis`. For multi‑step requests I first write a todo list (`write_file`), then iterate through the list, calling `api_resolver` for each row.
3. **Parameter collection** – The sub‑agent is responsible for prompting you (or extracting from the conversation) for any required fields (e.g., `customer_id`, `amount`). I only decide *which* API to use; the sub‑agent handles the exact payload.
4. **No extra logic** – I do not modify the API payload or response; I merely pass it through and then explain it in plain language.
5. **Status tracking** – After each successful call I mark the task as `completed` (or `failed` if something went wrong) in the todo file so you can see the progress.

#### Simple example workflow

1. **User request:** “Create a new ticket for user Jane with priority high.”  
2. **I run `get_apis`** → returns an API called `create_ticket`.  
3. **I prepare the task description** → `"Create a support ticket for Jane with high priority"`.  
4. **I invoke `api_resolver`**:  

   ```json
   {
     "selected_api": "create_ticket",
     "task": "Create a support ticket for Jane with high priority"
   }
   ```
5. **Sub‑agent asks for parameters** (e.g., `user_name`, `priority`) → I provide them from the conversation.  
6. **Sub‑agent calls the real service** → gets `{ "ticket_id": "T-1024", "status": "open" }`.  
7. **I reply to you:** “Ticket **T‑1024** has been opened for Jane with high priority.”  
8. **If this were part of a multi‑step process**, I’d also edit the todo file to mark the `create_ticket` row as completed and store the JSON response.

That’s the whole lifecycle of the sub‑agent: discover the API, hand it off with a clear purpose, let it gather parameters and call the service, then translate the result back for you while updating any execution plan. Let me know if you’d like to see a live demonstration of a specific API call!