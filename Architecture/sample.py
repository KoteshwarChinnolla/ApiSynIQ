from dataclasses import dataclass, field
from typing import List, Dict, Optional


@dataclass
class DescribeDto:
    name: Optional[str] = None
    description: Optional[str] = None
    dataType: Optional[str] = None
    defaultValue: Optional[str] = None
    options: Optional[str] = None
    autoExecute: Optional[bool] = None
    example: Optional[str] = None


@dataclass
class DtoSchema:
    className: Optional[str] = None
    name: Optional[str] = None
    description: Optional[str] = None
    example: Optional[str] = None
    fields: List[DescribeDto] = field(default_factory=list)


@dataclass
class InputsDto:
    inputBody: Dict[str, str] = field(default_factory=dict)
    inputPathParams: Dict[str, str] = field(default_factory=dict)
    inputQueryParams: Dict[str, str] = field(default_factory=dict)
    inputVariables: Dict[str, str] = field(default_factory=dict)
    inputHeaders: Dict[str, str] = field(default_factory=dict)
    inputCookies: Dict[str, str] = field(default_factory=dict)


@dataclass
class InputData:
    name: Optional[str] = None
    endpoint: Optional[str] = None
    httpMethod: Optional[str] = None
    tags: List[str] = field(default_factory=list)
    description: Optional[str] = None
    returnDescription: Optional[str] = None
    inputsDescribe: Optional[InputsDto] = None
    responseBody: Optional[str] = None
    autoExecute: Optional[bool] = None
    inputs: Optional[InputsDto] = None
    outputBody: Optional[str] = None
    filteringTags: List[str] = field(default_factory=list)
    dtoSchemas: Dict[str, DtoSchema] = field(default_factory=dict)
    describeDtosForParms: Dict[str, DescribeDto] = field(default_factory=dict)


example = InputData(
    name="Details",
    endpoint="advanced/employee/primary/{employeeName}",
    httpMethod="POST",
    tags=["Employee", "Post", "Details", "fill", "JSON"],
    description=(
        "A form to fill or post all the primary details of an employee, used when "
        "the employee first enter the company. This include all the primary details "
        "of the employee which essential for employee management"
    ),
    returnDescription="Returns the Name and Id of the employee",
    inputsDescribe=InputsDto(
        inputBody={"requestBody": "\"EmployeeInfo\""},
        inputPathParams={"employeeName": "java.lang.String"},
        inputQueryParams={"employeeId": "java.lang.String"},
        inputVariables={},
        inputHeaders={},
        inputCookies={}
    ),
    responseBody="Profile",
    autoExecute=True,
    inputs=InputsDto(
        inputBody={
            "requestBody": "{\"address\":{\"city\":\"String\",\"state\":\"String\"},\"name\":\"String\",\"age\":123}"
        },
        inputPathParams={"employeeName": "\"String\""},
        inputQueryParams={"employeeId": "\"String\""},
        inputVariables={},
        inputHeaders={},
        inputCookies={}
    ),
    outputBody="{name=String, id=123}",
    filteringTags=["all", "aiExposeController", "aiExposeEpHttp"],
    dtoSchemas={
        "EmployeeInfo": DtoSchema(
            className="AIExpose.Agent.Dtos.EmployeeInfo",
            name="EmployeeInfo",
            description=(
                "Consists of major details of a particulate employee, can be used when "
                "creating a new employee, updating a employee or have to get the major "
                "details of the employee"
            ),
            example="",
            fields=[
                DescribeDto(
                    name="Name",
                    description=(
                        "Full name of the employee, must be conformed before directly executing"
                    ),
                    dataType="String",
                    defaultValue="",
                    options="",
                    autoExecute=False,
                    example=""
                ),
                DescribeDto(
                    name="age",
                    description="Age of the user",
                    dataType="Integer",
                    defaultValue="20",
                    options="",
                    autoExecute=True,
                    example=""
                ),
                DescribeDto(
                    name="address",
                    description="No description provided.",
                    dataType="address",
                    defaultValue="No default value provided.",
                    options="No options provided.",
                    autoExecute=True,
                    example="No example provided."
                )
            ]
        ),
        "address": DtoSchema(
            className="AIExpose.Agent.Dtos.address",
            name="",
            description="",
            example="",
            fields=[
                DescribeDto(
                    name="",
                    description="Must be provided with the pin code",
                    dataType="String",
                    defaultValue="",
                    options="",
                    autoExecute=True,
                    example=""
                ),
                DescribeDto(
                    name="state",
                    description="No description provided.",
                    dataType="String",
                    defaultValue="No default value provided.",
                    options="No options provided.",
                    autoExecute=True,
                    example="No example provided."
                )
            ]
        ),
        "Profile": DtoSchema(
            className="AIExpose.Agent.Dtos.Profile",
            name="SampleDto3",
            description="This is a sample DTO 3",
            example='{"fieldA": "ExampleA","fieldB": 123}',
            fields=[
                DescribeDto(
                    name="Field A",
                    description="This is field A",
                    dataType="String",
                    defaultValue="",
                    options="",
                    autoExecute=False,
                    example="ExampleA"
                ),
                DescribeDto(
                    name="Field B",
                    description="This is field B",
                    dataType="int",
                    defaultValue="",
                    options="",
                    autoExecute=False,
                    example="123"
                )
            ]
        )
    },
    describeDtosForParms={
        "employeeName": DescribeDto(
            name="employeeName",
            description="Full name of the employee",
            dataType="String",
            defaultValue="",
            options="",
            autoExecute=False,
            example="Koteshwar"
        ),
        "employeeId": DescribeDto(
            name="employeeId",
            description="Id of the employee which will be unique amound all the employees",
            dataType="String",
            defaultValue="",
            options="",
            autoExecute=False,
            example="ACS0000001"
        )
    }
)

print(json.dumps(example, indent=4))
