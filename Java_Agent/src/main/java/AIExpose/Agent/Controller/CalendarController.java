package AIExpose.Agent.Controller;


import AIExpose.Agent.Annotations.*;
import AIExpose.Agent.Samples.CalendarDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.*;

@RestController
@RequestMapping("/calendar")
@AIExposeController
public class CalendarController {

    @AIExposeEpHttp(
            name = "Get Calendar Entry",
            description = "Fetches a calendar record for a specific employee on a given date.",
            autoExecute = true,
            tags = {"Calendar", "Get", "Employee", "Schedule"},
            pathParams = @Describe(name = "employeeId", description = "Employee ID whose calendar entry is requested.", dataType = "String", example = "EMP1001"),
            reqParams = @Describe(name = "date", description = "Date for which calendar entry is needed.", dataType = "String (yyyy-MM-dd)", example = "2025-11-09"),
            returnDescription = "Returns a CalendarDto containing event, attendance, and working hours details."
    )
    @GetMapping("/{employeeId}")
    public ResponseEntity<CalendarDto> getCalendar(@PathVariable String employeeId, @RequestParam String date) {
        CalendarDto dto = CalendarDto.builder()
                .id(1L)
                .date(LocalDate.parse(date))
                .employeeId(employeeId)
                .event("Project Standup Meeting")
                .isPresent("YES")
                .day("Monday")
                .holiday(false)
                .login(LocalTime.of(9, 0))
                .logout(LocalTime.of(17, 30))
                .mode("Office")
                .build();
        return ResponseEntity.ok(dto);
    }

    @AIExposeEpHttp(
            name = "Create Calendar Entry",
            description = "Creates a new calendar record for an employee, including attendance and event details.",
            autoExecute = true,
            tags = {"Calendar", "Post", "Create"},
            returnDescription = "Returns a confirmation message for successful calendar record creation."
    )
    @PostMapping
    public ResponseEntity<String> createCalendar(@RequestBody CalendarDto calendar) {
        return ResponseEntity.ok("Calendar entry created successfully for Employee ID: " + calendar.getEmployeeId());
    }

    @AIExposeEpHttp(
            name = "Update Calendar Entry",
            description = "Updates an existing calendar entry for an employee. Used for modifying login/logout times or updating event information.",
            autoExecute = true,
            tags = {"Calendar", "Put", "Update"},
            pathParams = @Describe(name = "id", description = "ID of the calendar entry to update.", dataType = "Long", example = "5"),
            returnDescription = "Returns a confirmation message after successful update."
    )
    @PutMapping("/{id}")
    public ResponseEntity<String> updateCalendar(@PathVariable Long id, @RequestBody CalendarDto updatedCalendar) {
        return ResponseEntity.ok("Calendar entry with ID " + id + " updated successfully.");
    }

    @AIExposeEpHttp(
            name = "Delete Calendar Entry",
            description = "Deletes a calendar record for a specific employee or date.",
            autoExecute = true,
            tags = {"Calendar", "Delete", "Remove"},
            pathParams = @Describe(name = "id", description = "ID of the calendar record to delete.", dataType = "Long", example = "7"),
            returnDescription = "Returns confirmation that the calendar entry was deleted."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCalendar(@PathVariable Long id) {
        return ResponseEntity.ok("Calendar entry with ID " + id + " deleted successfully.");
    }
}

