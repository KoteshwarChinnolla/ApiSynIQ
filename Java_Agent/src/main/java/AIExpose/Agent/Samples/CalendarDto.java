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
            description = "Unique identifier for the calendar record.",
            dataType = "Long",
            autoExecute = false,
            example = "1"
    )
    private Long id;

    @Describe(
            description = "The date this calendar entry represents.",
            dataType = "LocalDate",
            autoExecute = false,
            example = "2025-11-09"
    )
    private LocalDate date;

    @Describe(
            description = "Identifier of the employee for whom this calendar record applies.",
            dataType = "String",
            autoExecute = false,
            example = "EMP1001"
    )
    private String employeeId;

    @Describe(
            description = "Total effective working duration after considering breaks and exceptions.",
            dataType = "Duration",
            autoExecute = false,
            example = "PT7H30M"
    )
    private Duration effectiveHours;

    @Describe(
            description = "Indicates whether the employee was present, absent, or half-day.",
            dataType = "String",
            autoExecute = false,
            example = "YES"
    )
    private String isPresent;

    @Describe(
            description = "Event scheduled for the day, such as a meeting or training session.",
            dataType = "String",
            autoExecute = false,
            example = "Team Meeting"
    )
    private String event;

    @Describe(
            description = "Additional notes or summary for the day’s activities.",
            dataType = "String",
            autoExecute = false,
            example = "Project discussion with the AI team."
    )
    private String description;

    @Describe(
            description = "Day of the week corresponding to this date.",
            dataType = "String",
            autoExecute = false,
            example = "Monday"
    )
    private String day;

    @Describe(
            description = "Indicates whether the day is marked as a holiday.",
            dataType = "boolean",
            autoExecute = false,
            example = "false"
    )
    private boolean holiday;

    @Describe(
            description = "The time when the employee logged in or checked in for work.",
            dataType = "LocalTime",
            autoExecute = false,
            example = "09:00:00"
    )
    private LocalTime login;

    @Describe(
            description = "The time when the employee logged out or checked out from work.",
            dataType = "LocalTime",
            autoExecute = false,
            example = "17:30:00"
    )
    private LocalTime logout;

    @Describe(
            description = "Specifies whether the employee worked from office, home, or field.",
            dataType = "String",
            autoExecute = false,
            example = "Office"
    )
    private String mode;

    @Describe(
            description = "Total working hours before applying deductions.",
            dataType = "Duration",
            autoExecute = false,
            example = "PT8H30M"
    )
    private Duration grossHours;

    @Describe(
            description = "Whether the employee was present in the first half of the day.",
            dataType = "Boolean",
            autoExecute = false,
            example = "true"
    )
    private Boolean firstSection;

    @Describe(
            description = "Whether the employee was present in the second half of the day.",
            dataType = "Boolean",
            autoExecute = false,
            example = "true"
    )
    private Boolean secondSection;

    @Describe(
            description = "If true, marks the employee as present for attendance records even with exceptions.",
            dataType = "Boolean",
            autoExecute = false,
            example = "true"
    )
    private Boolean considerPresent;
}
