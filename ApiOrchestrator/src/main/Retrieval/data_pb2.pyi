from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class EndpointData(_message.Message):
    __slots__ = ("name", "endpoint", "httpMethod", "tags", "description", "returnDescription", "inputsDescribe", "responseBody", "autoExecute", "inputs", "outputBody", "filteringTags", "dtoSchemas", "describeDtosForParms", "globalPath")
    class DtoSchemasEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: DtoSchema
        def __init__(self, key: _Optional[str] = ..., value: _Optional[_Union[DtoSchema, _Mapping]] = ...) -> None: ...
    class DescribeDtosForParmsEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: Describe
        def __init__(self, key: _Optional[str] = ..., value: _Optional[_Union[Describe, _Mapping]] = ...) -> None: ...
    NAME_FIELD_NUMBER: _ClassVar[int]
    ENDPOINT_FIELD_NUMBER: _ClassVar[int]
    HTTPMETHOD_FIELD_NUMBER: _ClassVar[int]
    TAGS_FIELD_NUMBER: _ClassVar[int]
    DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    RETURNDESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    INPUTSDESCRIBE_FIELD_NUMBER: _ClassVar[int]
    RESPONSEBODY_FIELD_NUMBER: _ClassVar[int]
    AUTOEXECUTE_FIELD_NUMBER: _ClassVar[int]
    INPUTS_FIELD_NUMBER: _ClassVar[int]
    OUTPUTBODY_FIELD_NUMBER: _ClassVar[int]
    FILTERINGTAGS_FIELD_NUMBER: _ClassVar[int]
    DTOSCHEMAS_FIELD_NUMBER: _ClassVar[int]
    DESCRIBEDTOSFORPARMS_FIELD_NUMBER: _ClassVar[int]
    GLOBALPATH_FIELD_NUMBER: _ClassVar[int]
    name: str
    endpoint: str
    httpMethod: str
    tags: _containers.RepeatedScalarFieldContainer[str]
    description: str
    returnDescription: str
    inputsDescribe: Inputs
    responseBody: str
    autoExecute: bool
    inputs: Inputs
    outputBody: str
    filteringTags: _containers.RepeatedScalarFieldContainer[str]
    dtoSchemas: _containers.MessageMap[str, DtoSchema]
    describeDtosForParms: _containers.MessageMap[str, Describe]
    globalPath: str
    def __init__(self, name: _Optional[str] = ..., endpoint: _Optional[str] = ..., httpMethod: _Optional[str] = ..., tags: _Optional[_Iterable[str]] = ..., description: _Optional[str] = ..., returnDescription: _Optional[str] = ..., inputsDescribe: _Optional[_Union[Inputs, _Mapping]] = ..., responseBody: _Optional[str] = ..., autoExecute: bool = ..., inputs: _Optional[_Union[Inputs, _Mapping]] = ..., outputBody: _Optional[str] = ..., filteringTags: _Optional[_Iterable[str]] = ..., dtoSchemas: _Optional[_Mapping[str, DtoSchema]] = ..., describeDtosForParms: _Optional[_Mapping[str, Describe]] = ..., globalPath: _Optional[str] = ...) -> None: ...

class Inputs(_message.Message):
    __slots__ = ("inputBody", "inputPathParams", "inputQueryParams", "inputVariables", "inputHeaders", "inputCookies")
    class InputBodyEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: str
        def __init__(self, key: _Optional[str] = ..., value: _Optional[str] = ...) -> None: ...
    class InputPathParamsEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: str
        def __init__(self, key: _Optional[str] = ..., value: _Optional[str] = ...) -> None: ...
    class InputQueryParamsEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: str
        def __init__(self, key: _Optional[str] = ..., value: _Optional[str] = ...) -> None: ...
    class InputVariablesEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: str
        def __init__(self, key: _Optional[str] = ..., value: _Optional[str] = ...) -> None: ...
    class InputHeadersEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: str
        def __init__(self, key: _Optional[str] = ..., value: _Optional[str] = ...) -> None: ...
    class InputCookiesEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: str
        def __init__(self, key: _Optional[str] = ..., value: _Optional[str] = ...) -> None: ...
    INPUTBODY_FIELD_NUMBER: _ClassVar[int]
    INPUTPATHPARAMS_FIELD_NUMBER: _ClassVar[int]
    INPUTQUERYPARAMS_FIELD_NUMBER: _ClassVar[int]
    INPUTVARIABLES_FIELD_NUMBER: _ClassVar[int]
    INPUTHEADERS_FIELD_NUMBER: _ClassVar[int]
    INPUTCOOKIES_FIELD_NUMBER: _ClassVar[int]
    inputBody: _containers.ScalarMap[str, str]
    inputPathParams: _containers.ScalarMap[str, str]
    inputQueryParams: _containers.ScalarMap[str, str]
    inputVariables: _containers.ScalarMap[str, str]
    inputHeaders: _containers.ScalarMap[str, str]
    inputCookies: _containers.ScalarMap[str, str]
    def __init__(self, inputBody: _Optional[_Mapping[str, str]] = ..., inputPathParams: _Optional[_Mapping[str, str]] = ..., inputQueryParams: _Optional[_Mapping[str, str]] = ..., inputVariables: _Optional[_Mapping[str, str]] = ..., inputHeaders: _Optional[_Mapping[str, str]] = ..., inputCookies: _Optional[_Mapping[str, str]] = ...) -> None: ...

class DtoSchema(_message.Message):
    __slots__ = ("className", "name", "description", "example", "fields")
    CLASSNAME_FIELD_NUMBER: _ClassVar[int]
    NAME_FIELD_NUMBER: _ClassVar[int]
    DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    EXAMPLE_FIELD_NUMBER: _ClassVar[int]
    FIELDS_FIELD_NUMBER: _ClassVar[int]
    className: str
    name: str
    description: str
    example: str
    fields: _containers.RepeatedCompositeFieldContainer[Describe]
    def __init__(self, className: _Optional[str] = ..., name: _Optional[str] = ..., description: _Optional[str] = ..., example: _Optional[str] = ..., fields: _Optional[_Iterable[_Union[Describe, _Mapping]]] = ...) -> None: ...

class Describe(_message.Message):
    __slots__ = ("name", "description", "dataType", "defaultValue", "options", "autoExecute", "example")
    NAME_FIELD_NUMBER: _ClassVar[int]
    DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
    DATATYPE_FIELD_NUMBER: _ClassVar[int]
    DEFAULTVALUE_FIELD_NUMBER: _ClassVar[int]
    OPTIONS_FIELD_NUMBER: _ClassVar[int]
    AUTOEXECUTE_FIELD_NUMBER: _ClassVar[int]
    EXAMPLE_FIELD_NUMBER: _ClassVar[int]
    name: str
    description: str
    dataType: str
    defaultValue: str
    options: str
    autoExecute: bool
    example: str
    def __init__(self, name: _Optional[str] = ..., description: _Optional[str] = ..., dataType: _Optional[str] = ..., defaultValue: _Optional[str] = ..., options: _Optional[str] = ..., autoExecute: bool = ..., example: _Optional[str] = ...) -> None: ...

class InputsAndReturnsMatch(_message.Message):
    __slots__ = ("inputsMatchData", "returnMatchData")
    INPUTSMATCHDATA_FIELD_NUMBER: _ClassVar[int]
    RETURNMATCHDATA_FIELD_NUMBER: _ClassVar[int]
    inputsMatchData: _containers.RepeatedCompositeFieldContainer[EndpointData]
    returnMatchData: _containers.RepeatedCompositeFieldContainer[EndpointData]
    def __init__(self, inputsMatchData: _Optional[_Iterable[_Union[EndpointData, _Mapping]]] = ..., returnMatchData: _Optional[_Iterable[_Union[EndpointData, _Mapping]]] = ...) -> None: ...

class repeatedInput(_message.Message):
    __slots__ = ("inputs",)
    INPUTS_FIELD_NUMBER: _ClassVar[int]
    inputs: _containers.RepeatedCompositeFieldContainer[EndpointData]
    def __init__(self, inputs: _Optional[_Iterable[_Union[EndpointData, _Mapping]]] = ...) -> None: ...

class query(_message.Message):
    __slots__ = ("query", "limit")
    QUERY_FIELD_NUMBER: _ClassVar[int]
    LIMIT_FIELD_NUMBER: _ClassVar[int]
    query: str
    limit: int
    def __init__(self, query: _Optional[str] = ..., limit: _Optional[int] = ...) -> None: ...
