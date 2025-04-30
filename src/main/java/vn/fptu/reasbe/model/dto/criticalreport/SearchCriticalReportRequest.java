package vn.fptu.reasbe.model.dto.criticalreport;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import vn.fptu.reasbe.model.enums.criticalreport.StatusCriticalReport;
import vn.fptu.reasbe.model.enums.criticalreport.TypeCriticalReport;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchCriticalReportRequest {
    List<Integer> ids;
    List<TypeCriticalReport> typeReports;
    String userFullName;
    List<Integer> residentIds;
    List<Integer> feedbackIds;
    List<Integer> exchangeRequestIds;
    String reporterName;
    List<Integer> reporterIds;
    String answererName;
    List<Integer> answererIds;
    List<StatusCriticalReport> statusCriticalReports;
}
