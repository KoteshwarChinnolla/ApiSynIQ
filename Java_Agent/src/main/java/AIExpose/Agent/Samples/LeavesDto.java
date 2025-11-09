package AIExpose.Agent.Samples;


import AIExpose.Agent.Annotations.*;
import lombok.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@AIExposeDto(
        name = "LeavesDto",
        description = "DTO representing a leave application by an employee. Includes leave type, duration, status, and remarks.",
        example = "{" +
                "\"id\": 1," +
                "\"employeeId\": 1001," +
                "\"isHalf_Day\": false," +
                "\"Leave_type\": \"SICK_LEAVE\"," +
                "\"Leave_on\": \"2025-11-10\"," +
                "\"status\": \"PENDING\"," +
                "\"Request_By\": \"John Doe\"," +
                "\"Details\": \"Fever and rest advised by doctor.\"," +
                "\"Action_Date\": null," +
                "\"Rejection_Reason\": null," +
                "\"Action\": null" +
                "}"
)
public class LeavesDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Describe(
            name = "Leave ID",
            description = "Unique identifier for the leave request.",
            dataType = "Long",
            autoExecute = false,
            example = "1"
    )
    private Long id;

    @Describe(
            name = "Employee ID",
            description = "Identifier of the employee applying for leave.",
            dataType = "Long",
            autoExecute = false,
            example = "1001"
    )
    private Long employeeId;

    @Describe(
            name = "Half Day Leave",
            description = "Indicates whether the leave is for a half day.",
            dataType = "boolean",
            autoExecute = false,
            example = "false"
    )
    private boolean isHalf_Day;

    @Describe(
            name = "Leave Type",
            description = "Specifies the type of leave such as SICK, CASUAL, or PAID.",
            dataType = "String",
            autoExecute = false,
            example = "SICK_LEAVE"
    )
    private String Leave_type;

    @Describe(
            name = "Leave Date",
            description = "The date on which the leave is requested.",
            dataType = "String (ISO Date Format)",
            autoExecute = false,
            example = "2025-11-10"
    )
    private String Leave_on;

    @Describe(
            name = "Leave Status",
            description = "Current status of the leave request (e.g., APPROVED, PENDING, REJECTED).",
            dataType = "String",
            autoExecute = false,
            example = "PENDING"
    )
    private String status;

    @Describe(
            name = "Requested By",
            description = "Name of the employee who applied for the leave.",
            dataType = "String",
            autoExecute = false,
            example = "John Doe"
    )
    private String Request_By;

    @Describe(
            name = "Leave Details",
            description = "Additional remarks or reason for taking leave.",
            dataType = "String",
            autoExecute = false,
            example = "Fever and rest advised by doctor."
    )
    private String Details;

    @Describe(
            name = "Action Date",
            description = "Date on which the manager or HR took action on the leave request.",
            dataType = "String (ISO Date Format)",
            autoExecute = false,
            example = "2025-11-11"
    )
    private String Action_Date;

    @Describe(
            name = "Rejection Reason",
            description = "If the leave is rejected, the reason for rejection is mentioned here.",
            dataType = "String",
            autoExecute = false,
            example = "Leave quota exhausted."
    )
    private String Rejection_Reason;

    @Describe(
            name = "Action Taken",
            description = "Specifies the action taken on the leave request such as APPROVE or REJECT.",
            dataType = "String",
            autoExecute = false,
            example = "APPROVE"
    )
    private String Action;
}

