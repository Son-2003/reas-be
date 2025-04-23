package vn.fptu.reasbe.service;

import vn.fptu.reasbe.model.dto.core.BaseSearchPaginationResponse;
import vn.fptu.reasbe.model.dto.criticalreport.CriticalReportResidentRequest;
import vn.fptu.reasbe.model.dto.criticalreport.CriticalReportResponse;
import vn.fptu.reasbe.model.dto.criticalreport.CriticalReportStaffRequest;
import vn.fptu.reasbe.model.dto.criticalreport.SearchCriticalReportRequest;

public interface CriticalReportService {
    BaseSearchPaginationResponse<CriticalReportResponse> searchCriticalReport(int pageSize, int pageNo, String sortDir, String sortBy, SearchCriticalReportRequest searchRequest);

    CriticalReportResponse getCriticalReport(Integer id);

    CriticalReportResponse createCriticalReport(CriticalReportResidentRequest request);

    CriticalReportResponse reviewCriticalReport(CriticalReportStaffRequest request);
}
