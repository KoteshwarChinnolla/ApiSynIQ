package AIExpose.Agent.Controller;


import AIExpose.Agent.Annotations.*;
import AIExpose.Agent.Samples.LeavesDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/leaves")
@AIExposeController
public class LeavesController {

    @AIExposeEpHttp(
            name = "Apply Leave",
            description = "Endpoint to submit a new leave request. Employees can apply for paid, unpaid, casual, or sick leave with appropriate details.",
            autoExecute = true,
            tags = {"Leaves", "Post", "Employee", "Apply", "JSON"},
            reqParams = @Describe(name = "employeeId", description = "Employee ID for whom the leave is applied.", dataType = "Long", example = "1001"),
            returnDescription = "Returns a success message confirming the leave application submission."
    )
    @PostMapping("/apply")
    public ResponseEntity<String> applyLeave(@RequestBody LeavesDto leaveRequest, @RequestParam Long employeeId) {
        return ResponseEntity.ok("Leave application submitted successfully for Employee ID: " + employeeId);
    }

    @AIExposeEpHttp(
            name = "Get Leave Details",
            description = "Fetches the leave details for a specific leave ID. Useful for viewing the status and details of an existing leave request.",
            autoExecute = true,
            tags = {"Leaves", "Get", "Fetch", "Details"},
            pathParams = @Describe(name = "id", description = "Unique ID of the leave record.", dataType = "Long", example = "1"),
            returnDescription = "Returns the complete details of the leave with the provided ID."
    )
    @GetMapping("/{id}")
    public ResponseEntity<LeavesDto> getLeave(@PathVariable Long id) {
        LeavesDto leave = new LeavesDto();
        leave.setId(id);
        leave.setEmployeeId(1001L);
        leave.setDetails("Sick leave for two days");
        return ResponseEntity.ok(leave);
    }

    @AIExposeEpHttp(
            name = "Update Leave",
            description = "Allows an employee or manager to modify an existing leave request, such as changing the date or updating the status.",
            autoExecute = true,
            tags = {"Leaves", "Put", "Update"},
            pathParams = @Describe(name = "id", description = "Leave ID to update.", dataType = "Long", example = "2"),
            returnDescription = "Returns a message confirming that the leave details were updated successfully."
    )
    @PutMapping("/{id}")
    public ResponseEntity<String> updateLeave(@PathVariable Long id, @RequestBody LeavesDto updatedLeave) {
        return ResponseEntity.ok("Leave with ID " + id + " updated successfully.");
    }

    @AIExposeEpHttp(
            name = "Delete Leave",
            description = "Deletes a leave record by ID. Useful for cancelling a leave request before approval or for administrative cleanup.",
            autoExecute = true,
            tags = {"Leaves", "Delete", "Remove"},
            pathParams = @Describe(name = "id", description = "Unique leave ID to delete.", dataType = "Long", example = "3"),
            returnDescription = "Returns a confirmation message after successful deletion."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteLeave(@PathVariable Long id) {
        return ResponseEntity.ok("Leave with ID " + id + " deleted successfully.");
    }
}

