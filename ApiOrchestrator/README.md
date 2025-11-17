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