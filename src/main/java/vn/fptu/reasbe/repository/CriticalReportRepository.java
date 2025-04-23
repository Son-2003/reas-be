package vn.fptu.reasbe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import vn.fptu.reasbe.model.entity.CriticalReport;
import vn.fptu.reasbe.repository.custom.CriticalReportRepositoryCustom;

public interface CriticalReportRepository extends JpaRepository<CriticalReport, Integer>, QuerydslPredicateExecutor<CriticalReport>, CriticalReportRepositoryCustom {
}
