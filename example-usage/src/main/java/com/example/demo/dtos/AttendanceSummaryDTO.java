package AIExpose.Agent.Samples;


import AIExpose.Agent.Annotations.*;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@AIExposeDto(
        name = "AttendanceSummaryDTO",
        description = "Represents a daily summary of an employee's attendance, including presence, absence, and categorized leave counts.",
        example = "{" +
                "\"date\": \"2025-11-09\"," +
                "\"present\": 8," +
                "\"absent\": 2," +
                "\"paidApprovedLeaves\": 1," +
                "\"paidUnapprovedLeaves\": 0," +
                "\"unpaidApprovedLeaves\": 0," +
                "\"unpaidUnapprovedLeaves\": 1," +
                "\"sickLeaves\": 1," +
                "\"sickUnapprovedLeaves\": 0," +
                "\"casualApprovedLeaves\": 2," +
                "\"casualUnapprovedLeaves\": 0," +
                "\"approvedLeaves\": 3," +
                "\"pendingLeaves\": 1" +
                "}"
)
public class AttendanceSummaryDTO {

    @Describe(
            description = "The date for which the attendance summary is recorded.",
            dataType = "LocalDate",
            autoExecute = false,
            example = "2025-11-09"
    )
    private LocalDate date;

    @Describe(
            description = "Number of employees marked as present on this date.",
            dataType = "int",
            autoExecute = false,
            example = "8"
    )
    private int present;

    @Describe(
            description = "Number of employees marked as absent on this date.",
            dataType = "int",
            autoExecute = false,
            example = "2"
    )
    private int absent;

    @Describe(
            description = "Count of leaves that were both paid and approved.",
            dataType = "int",
            autoExecute = false,
            example = "1"
    )
    private int paidApprovedLeaves;

    @Describe(
            description = "Count of paid leaves that have not yet been approved.",
            dataType = "int",
            autoExecute = false,
            example = "0"
    )
    private int paidUnapprovedLeaves;

    @Describe(
            description = "Count of unpaid leaves that were approved.",
            dataType = "int",
            autoExecute = false,
            example = "0"
    )
    private int unpaidApprovedLeaves;

    @Describe(
            description = "Count of unpaid leaves still pending approval.",
            dataType = "int",
            autoExecute = false,
            example = "1"
    )
    private int unpaidUnapprovedLeaves;

    @Describe(
            description = "Number of approved sick leaves taken on this date.",
            dataType = "int",
            autoExecute = false,
            example = "1"
    )
    private int sickLeaves;

    @Describe(
            description = "Number of sick leaves not yet approved.",
            dataType = "int",
            autoExecute = false,
            example = "0"
    )
    private int sickUnapprovedLeaves;

    @Describe(
            description = "Number of approved casual leaves taken on this date.",
            dataType = "int",
            autoExecute = false,
            example = "2"
    )
    private int casualApprovedLeaves;

    @Describe(
            description = "Number of casual leaves not yet approved.",
            dataType = "int",
            autoExecute = false,
            example = "0"
    )
    private int casualUnapprovedLeaves;

    @Describe(
            description = "Total number of approved leaves (of all types).",
            dataType = "int",
            autoExecute = false,
            example = "3"
    )
    private int approvedLeaves;

    @Describe(
            description = "Total number of leave requests pending approval.",
            dataType = "int",
            autoExecute = false,
            example = "1"
    )
    private int pendingLeaves;
}
