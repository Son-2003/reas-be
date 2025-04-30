package vn.fptu.reasbe.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.fptu.reasbe.model.constant.AppConstants;
import vn.fptu.reasbe.model.dto.core.BaseSearchPaginationResponse;
import vn.fptu.reasbe.model.dto.criticalreport.CriticalReportResidentRequest;
import vn.fptu.reasbe.model.dto.criticalreport.CriticalReportResponse;
import vn.fptu.reasbe.model.dto.criticalreport.CriticalReportStaffRequest;
import vn.fptu.reasbe.model.dto.criticalreport.SearchCriticalReportRequest;
import vn.fptu.reasbe.service.CriticalReportService;

@RestController
@RequestMapping("/api/v1/critical-report")
@RequiredArgsConstructor
public class CriticalReportController {
    private final CriticalReportService criticalReportService;

    @PostMapping("/search")
    @PreAuthorize("hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_STAFF) " +
            "or hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_ADMIN)" +
            "or hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_RESIDENT)")
    public ResponseEntity<BaseSearchPaginationResponse<CriticalReportResponse>> searchCriticalReport(
            @RequestParam(name = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(name = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDir,
            @RequestBody @Nullable SearchCriticalReportRequest searchCriticalReportRequest
    ) {
        return ResponseEntity.ok(criticalReportService.searchCriticalReport(pageSize, pageNo, sortBy, sortDir, searchCriticalReportRequest));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_STAFF) " +
            "or hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_ADMIN)" +
            "or hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_RESIDENT)" )
    public ResponseEntity<CriticalReportResponse> getCriticalReportDetail(@PathVariable Integer id) {
        return ResponseEntity.ok(criticalReportService.getCriticalReport(id));
    }

    @PostMapping
    @PreAuthorize("hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_RESIDENT)")
    public ResponseEntity<CriticalReportResponse> createCriticalReport(@RequestBody @Valid CriticalReportResidentRequest request) {
        return ResponseEntity.ok(criticalReportService.createCriticalReport(request));
    }

    @PutMapping
    @PreAuthorize("hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_STAFF)"+
            "or hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_ADMIN)")
    public ResponseEntity<CriticalReportResponse> reviewCriticalReport(@RequestBody @Valid CriticalReportStaffRequest request) {
        return ResponseEntity.ok(criticalReportService.reviewCriticalReport(request));
    }
}
