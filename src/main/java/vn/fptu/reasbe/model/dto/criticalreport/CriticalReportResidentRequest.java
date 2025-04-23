package vn.fptu.reasbe.model.dto.criticalreport;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import vn.fptu.reasbe.model.enums.criticalreport.TypeCriticalReport;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CriticalReportResidentRequest {
    @NotNull(message = "TypeReport must not be blank")
    TypeCriticalReport typeReport;

    @NotBlank(message = "Content must not be blank")
    String contentReport;

    String imageUrl;

    Integer userId;

    Integer feedbackId;

    Integer exchangeId;
}
