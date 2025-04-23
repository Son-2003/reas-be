package vn.fptu.reasbe.repository.custom;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.fptu.reasbe.model.dto.criticalreport.SearchCriticalReportRequest;
import vn.fptu.reasbe.model.entity.CriticalReport;
import vn.fptu.reasbe.model.entity.ExchangeRequest;

import java.util.Optional;

public interface CriticalReportRepositoryCustom {
    Page<CriticalReport> searchCriticalReportPagination(SearchCriticalReportRequest request, Pageable pageable);
    CriticalReport findByExchange(ExchangeRequest exchange);
    Optional<CriticalReport> findCriticalReportById(Integer criticalReportId);
}
