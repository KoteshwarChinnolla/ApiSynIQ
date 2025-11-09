package AIExpose.Agent.Controller;


import AIExpose.Agent.Annotations.*;
import AIExpose.Agent.Samples.AttendanceSummaryDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/attendance")
@AIExposeController
public class AttendanceController {

    @AIExposeEpHttp(
            name = "Get Attendance Summary",
            description = "Retrieves attendance summary for a particular date, showing present, absent, and leave counts.",
            autoExecute = true,
            tags = {"Attendance", "Get", "Summary", "Date"},
            reqParams = @Describe(name = "date", description = "Date for which attendance summary is requested.", dataType = "String (yyyy-MM-dd)", example = "2025-11-09"),
            returnDescription = "Returns an AttendanceSummaryDTO containing attendance statistics for the requested date."
    )
    @GetMapping("/summary")
    public ResponseEntity<AttendanceSummaryDTO> getSummary(@RequestParam String date) {
        AttendanceSummaryDTO summary = new AttendanceSummaryDTO(
                LocalDate.parse(date), 8, 2, 1, 0, 0, 1, 1, 0, 2, 0, 3, 1
        );
        return ResponseEntity.ok(summary);
    }

    @AIExposeEpHttp(
            name = "Post Attendance Summary",
            description = "Allows admins to post or record attendance summary for a specific date.",
            autoExecute = true,
            tags = {"Attendance", "Post", "Summary", "Record"},
            returnDescription = "Returns a success message confirming the attendance summary submission."
    )
    @PostMapping("/summary")
    public ResponseEntity<String> postSummary(@RequestBody AttendanceSummaryDTO summary) {
        return ResponseEntity.ok("Attendance summary for " + summary.getDate() + " recorded successfully.");
    }

    @AIExposeEpHttp(
            name = "Update Attendance Summary",
            description = "Updates existing attendance records for a date, such as correcting counts or adding new leave entries.",
            autoExecute = true,
            tags = {"Attendance", "Put", "Update"},
            reqParams = @Describe(name = "date", description = "The date for which the attendance record is being updated.", dataType = "String", example = "2025-11-09"),
            returnDescription = "Returns a message confirming that the attendance summary has been updated."
    )
    @PutMapping("/summary")
    public ResponseEntity<String> updateSummary(@RequestParam String date, @RequestBody AttendanceSummaryDTO updatedSummary) {
        return ResponseEntity.ok("Attendance summary for " + date + " updated successfully.");
    }

    @AIExposeEpHttp(
            name = "Delete Attendance Record",
            description = "Deletes an attendance summary record for a specific date.",
            autoExecute = true,
            tags = {"Attendance", "Delete", "Summary"},
            reqParams = @Describe(name = "date", description = "Date whose attendance summary should be deleted.", dataType = "String", example = "2025-11-09"),
            returnDescription = "Returns confirmation of the deleted attendance record."
    )
    @DeleteMapping("/summary")
    public ResponseEntity<String> deleteSummary(@RequestParam String date) {
        return ResponseEntity.ok("Attendance summary for " + date + " deleted successfully.");
    }
}

