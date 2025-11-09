package AIExpose.Agent.Samples;


import AIExpose.Agent.Annotations.*;
import lombok.*;
import java.time.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@AIExposeDto(
        name = "CalendarDto",
        description = "DTO representing an employee’s daily calendar entry, including attendance, events, working hours, and presence information.",
        example = "{" +
                "\"id\": 1," +
                "\"date\": \"2025-11-09\"," +
                "\"employeeId\": \"EMP1001\"," +
                "\"effectiveHours\": \"PT7H30M\"," +
                "\"isPresent\": \"YES\"," +
                "\"event\": \"Team Meeting\"," +
                "\"description\": \"Project discussion with the AI team.\"," +
                "\"day\": \"Monday\"," +
                "\"holiday\": false," +
                "\"login\": \"09:00:00\"," +
                "\"logout\": \"17:30:00\"," +
                "\"mode\": \"Office\"," +
                "\"grossHours\": \"PT8H30M\"," +
                "\"firstSection\": true," +
                "\"secondSection\": true," +
                "\"considerPresent\": true" +
                "}"
)
public class CalendarDto {

    @Describe(
            name = "Calendar ID",
            description = "Unique identifier for the calendar record.",
            dataType = "Long",
            autoExecute = false,
            example = "1"
    )
    private Long id;

    @Describe(
            name = "Date",
            description = "The date this calendar entry represents.",
            dataType = "LocalDate",
            autoExecute = false,
            example = "2025-11-09"
    )
    private LocalDate date;

    @Describe(
            name = "Employee ID",
            description = "Identifier of the employee for whom this calendar record applies.",
            dataType = "String",
            autoExecute = false,
            example = "EMP1001"
    )
    private String employeeId;

    @Describe(
            name = "Effective Working Hours",
            description = "Total effective working duration after considering breaks and exceptions.",
            dataType = "Duration",
            autoExecute = false,
            example = "PT7H30M"
    )
    private Duration effectiveHours;

    @Describe(
            name = "Presence Status",
            description = "Indicates whether the employee was present, absent, or half-day.",
            dataType = "String",
            autoExecute = false,
            example = "YES"
    )
    private String isPresent;

    @Describe(
            name = "Event",
            description = "Event scheduled for the day, such as a meeting or training session.",
            dataType = "String",
            autoExecute = false,
            example = "Team Meeting"
    )
    private String event;

    @Describe(
            name = "Description",
            description = "Additional notes or summary for the day’s activities.",
            dataType = "String",
            autoExecute = false,
            example = "Project discussion with the AI team."
    )
    private String description;

    @Describe(
            name = "Day of Week",
            description = "Day of the week corresponding to this date.",
            dataType = "String",
            autoExecute = false,
            example = "Monday"
    )
    private String day;

    @Describe(
            name = "Is Holiday",
            description = "Indicates whether the day is marked as a holiday.",
            dataType = "boolean",
            autoExecute = false,
            example = "false"
    )
    private boolean holiday;

    @Describe(
            name = "Login Time",
            description = "The time when the employee logged in or checked in for work.",
            dataType = "LocalTime",
            autoExecute = false,
            example = "09:00:00"
    )
    private LocalTime login;

    @Describe(
            name = "Logout Time",
            description = "The time when the employee logged out or checked out from work.",
            dataType = "LocalTime",
            autoExecute = false,
            example = "17:30:00"
    )
    private LocalTime logout;

    @Describe(
            name = "Mode of Work",
            description = "Specifies whether the employee worked from office, home, or field.",
            dataType = "String",
            autoExecute = false,
            example = "Office"
    )
    private String mode;

    @Describe(
            name = "Gross Working Hours",
            description = "Total working hours before applying deductions.",
            dataType = "Duration",
            autoExecute = false,
            example = "PT8H30M"
    )
    private Duration grossHours;

    @Describe(
            name = "First Section Attendance",
            description = "Whether the employee was present in the first half of the day.",
            dataType = "Boolean",
            autoExecute = false,
            example = "true"
    )
    private Boolean firstSection;

    @Describe(
            name = "Second Section Attendance",
            description = "Whether the employee was present in the second half of the day.",
            dataType = "Boolean",
            autoExecute = false,
            example = "true"
    )
    private Boolean secondSection;

    @Describe(
            name = "Consider Present",
            description = "If true, marks the employee as present for attendance records even with exceptions.",
            dataType = "Boolean",
            autoExecute = false,
            example = "true"
    )
    private Boolean considerPresent;
}
