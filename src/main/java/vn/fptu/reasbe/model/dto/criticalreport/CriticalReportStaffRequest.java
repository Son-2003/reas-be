package vn.fptu.reasbe.model.dto.criticalreport;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CriticalReportStaffRequest {
    @NotNull(message = "Id must not be blank")
    Integer id;

    @NotBlank(message = "Response must not be blank")
    String contentResponse;

    @NotNull(message = "IsApproved must not be blank")
    Boolean isResolved;
}
