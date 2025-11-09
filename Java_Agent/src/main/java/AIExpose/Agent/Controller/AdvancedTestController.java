//package AIExpose.Agent.Controller;
//
//import AIExpose.Agent.Annotations.*;
//import AIExpose.Agent.Dtos.EmployeeInfo;
//import AIExpose.Agent.Dtos.EmployeeInfo;
//import AIExpose.Agent.Dtos.Profile;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.*;
//
//@RestController
//@RequestMapping("/advanced")
//@AIExposeController
//public class AdvancedTestController {
//
//    @AIExposeEpHttp(
//        name = "ComplexOrderHandler",
//        description = "Handles complex order creation with nested JSON and query params",
//        autoExecute = true,
//        tags = {"Order", "Complex"}
//    )
//    @PostMapping("/orders/{orderId}/process")
//    public ResponseEntity<Map<String, Object>> handleComplexOrder(
//            @PathVariable String orderId,
//            @RequestParam(required = false) String customerName,
//            @RequestParam(defaultValue = "medium") String priority,
//            @RequestHeader(value = "X-Source", required = false) String source,
//            @RequestBody(required = false) Map<String, Object> payload
//    ) {
//        Map<String, Object> response = new LinkedHashMap<>();
//        response.put("orderId", orderId);
//        response.put("customerName", customerName);
//        response.put("priority", priority);
//        response.put("source", source);
//        response.put("receivedPayload", payload);
//        response.put("timestamp", new Date());
//        return ResponseEntity.ok(response);
//    }
//
//    /**
//     * ✅ 2️⃣ Plain text request and response
//     * - Body is a raw string (no JSON)
//     */
//    @AIExposeEpHttp(
//        name = "PlainTextEcho",
//        description = "Echoes back raw plain text body",
//        autoExecute = true,
//        tags = {"Text", "Echo"}
//    )
//    @PostMapping(value = "/echo", consumes = "text/plain", produces = "text/plain")
//    public ResponseEntity<String> echoPlainText(@RequestBody String message) {
//        return ResponseEntity.ok("Received: " + message.trim().toUpperCase());
//    }
//
//    /**
//     * ✅ 3️⃣ Multipart upload + JSON field
//     * - Tests file uploads with additional JSON and string fields
//     */
//    @AIExposeEpHttp(
//        name = "UploadWithMeta",
//        description = "Handles file upload with metadata and JSON",
//        autoExecute = false,
//        tags = {"File", "Upload", "JSON"}
//    )
//    @PostMapping(value = "/upload", consumes = "multipart/form-data")
//    public ResponseEntity<Map<String, Object>> uploadFile(
//            @RequestParam("file") MultipartFile file,
//            @RequestParam("metadata") String metadata,
//            @RequestParam(value = "extra", required = false) String extra
//    ) {
//        Map<String, Object> response = new LinkedHashMap<>();
//        response.put("fileName", file.getOriginalFilename());
//        response.put("size", file.getSize());
//        response.put("metadata", metadata);
//        response.put("extra", extra);
//        return ResponseEntity.ok(response);
//    }
//
//    /**
//     * ✅ 4️⃣ Nested DTO inside List and Map
//     * - Deeply nested structure in body
//     */
//    @AIExposeEpHttp(
//        name = "Details",
//        description = "A form to fill or post all the primary details of an employee, used when the employee first enter the company. This include all the primary details of the employee which essential for employee management",
//        autoExecute = true,
//        tags = {"Employee", "Post", "Details", "fill", "JSON"},
//        pathParams=@Describe(name="employeeName", description="Full name of the employee", dataType="String", autoExecute=false, example = "Koteshwar"),
//        reqParams=@Describe(name="employeeId", description = "Id of the employee which will be unique amound all the employees", dataType="String", autoExecute=false, example="ACS0000001"),
//        returnDescription="Returns the Name and Id of the employee"
//    )
//    @PostMapping("/employee/primary/{employeeName}")
//    public ResponseEntity<Profile> nestedDtoHandler(
//            @RequestBody EmployeeInfo dtoMap, @PathVariable String employeeName, @RequestParam String employeeId
//    ) {
//        Profile sdto3 = new Profile();
//        sdto3.setName("fieldA");
//        sdto3.setId(2);
//        return ResponseEntity.ok(sdto3);
//    }
//
//    /**
//     * ✅ 5️⃣ Optional parameters and null-handling
//     * - Mix of optional query params, missing headers, and null body
//     */
//    @AIExposeEpHttp(
//        name = "OptionalParams",
//        description = "Handles missing, null, and optional values gracefully",
//        autoExecute = true,
//        tags = {"Optional", "NullSafe"}
//    )
//    @PostMapping("/optional/test")
//    public ResponseEntity<Map<String, Object>> optionalHandler(
//            @RequestParam(required = false) String key,
//            @RequestParam(required = false) Integer number,
//            @RequestHeader(value = "X-Test", required = false) String header,
//            @RequestBody(required = false) Map<String, Object> body
//    ) {
//        Map<String, Object> res = new LinkedHashMap<>();
//        res.put("key", key);
//        res.put("number", number);
//        res.put("header", header);
//        res.put("body", body);
//        res.put("nullSafe", "✅ Handled without errors");
//        return ResponseEntity.ok(res);
//    }
//
//    /**
//     * ✅ 6️⃣ GET with query filters (lists)
//     * - Tests list-type query params like ?tags=one&tags=two
//     */
//    @AIExposeEpHttp(
//        name = "ListQueryParams",
//        description = "Handles multiple query parameters with same key (List type)",
//        autoExecute = true,
//        tags = {"List", "Query"}
//    )
//    @GetMapping("/filter")
//    public ResponseEntity<Map<String, Object>> listQueryHandler(
//            @RequestParam List<String> tags
//    ) {
//        return ResponseEntity.ok(Map.of(
//                "tagsReceived", tags,
//                "count", tags.size()
//        ));
//    }
//
//    /**
//     * ✅ 7️⃣ Path param with slashes or special characters
//     * - Tests URL encoding and decoding edge cases
//     */
//    @AIExposeEpHttp(
//        name = "PathEdgeCase",
//        description = "Tests special characters and slashes in path variables",
//        autoExecute = false,
//        tags = {"Path", "Encoding"}
//    )
//    @GetMapping("/path/{wildPath:.+}")
//    public ResponseEntity<String> pathEdgeCase(@PathVariable("wildPath") String wildPath) {
//        return ResponseEntity.ok("Decoded Path: " + wildPath);
//    }
//
//    @AIExposeEpHttp
//    @PostMapping("/echo/json")
//    public ResponseEntity<EmployeeInfo> echoJson(@RequestBody EmployeeInfo message) {
//        return ResponseEntity.ok(message);
//    }
//}
