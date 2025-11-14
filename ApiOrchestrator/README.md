# Post Attendance Summary

**Method:** `POST`  **Endpoint:** `attendance/summary`  


**Is it has to be executed automatically:** `True`  

---

## ⚙️ Inputs

**Description:** Allows admins to post or record attendance summary for a specific date.  

### 🔸 Request Body
```json
{
    "date": "2025-11-12",
    "unpaidApprovedLeaves": 123,
    "unpaidUnapprovedLeaves": 123,
    "casualUnapprovedLeaves": 123,
    "sickLeaves": 123,
    "casualApprovedLeaves": 123,
    "approvedLeaves": 123,
    "sickUnapprovedLeaves": 123,
    "pendingLeaves": 123,
    "absent": 123,
    "present": 123,
    "paidApprovedLeaves": 123,
    "paidUnapprovedLeaves": 123
}
```
---

## 📦 Response

**Description**: Returns a success message confirming the attendance summary submission.

### 🔸 Response Body
```json
"String"
```

# Get Attendance Summary

**Method:** `GET`  **Endpoint:** `attendance/summary`


**Is it has to be executed automatically:** `True`

---

## ⚙️ Inputs

**Description:** Retrieves attendance summary for a particular date, showing present, absent, and leave counts.

### 🔸 Query Parameters
| date | "String" |

## 📦 Response

**Description**: Returns an AttendanceSummaryDTO containing attendance statistics for the requested date.

### 🔸 Response Body
```json
{
    "date": "2025-11-12",
    "unpaidApprovedLeaves": 123,
    "unpaidUnapprovedLeaves": 123,
    "casualUnapprovedLeaves": 123,
    "sickLeaves": 123,
    "casualApprovedLeaves": 123,
    "approvedLeaves": 123,
    "sickUnapprovedLeaves": 123,
    "pendingLeaves": 123,
    "absent": 123,
    "present": 123,
    "paidApprovedLeaves": 123,
    "paidUnapprovedLeaves": 123
}
```