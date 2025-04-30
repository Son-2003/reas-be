package vn.fptu.reasbe.repository.custom.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import vn.fptu.reasbe.model.dto.criticalreport.SearchCriticalReportRequest;
import vn.fptu.reasbe.model.entity.CriticalReport;
import vn.fptu.reasbe.model.entity.ExchangeRequest;
import vn.fptu.reasbe.model.entity.QCriticalReport;
import vn.fptu.reasbe.model.enums.core.StatusEntity;
import vn.fptu.reasbe.model.enums.criticalreport.StatusCriticalReport;
import vn.fptu.reasbe.repository.custom.CriticalReportRepositoryCustom;
import vn.fptu.reasbe.repository.custom.core.AbstractRepositoryCustom;

import java.util.Optional;

public class CriticalReportRepositoryCustomImpl extends AbstractRepositoryCustom<CriticalReport, QCriticalReport> implements CriticalReportRepositoryCustom {
    private static final Logger logger = Logger.getLogger(CriticalReportRepositoryCustomImpl.class);

    @Override
    protected QCriticalReport getEntityPath() {
        return QCriticalReport.criticalReport;
    }

    @Override
    protected <T> Expression<T> getIdPath(QCriticalReport entityPath) {
        return (Expression<T>) entityPath.id;
    }

    @Override
    protected BooleanBuilder buildFilter(Object requestObj, QCriticalReport entityPath) {
        QCriticalReport criticalReport = getEntityPath();
        BooleanBuilder builder = new BooleanBuilder();
        SearchCriticalReportRequest request = (SearchCriticalReportRequest) requestObj;

        if (request != null) {
            if (StringUtils.isNotBlank(request.getUserFullName())) {
                builder.and(criticalReport.resident.fullName.containsIgnoreCase(request.getUserFullName().trim()));
            }
            if (StringUtils.isNotBlank(request.getReporterName())) {
                builder.and(criticalReport.reporter.fullName.containsIgnoreCase(request.getReporterName().trim()));
            }
            if (StringUtils.isNotBlank(request.getAnswererName())) {
                builder.and(criticalReport.answerer.fullName.containsIgnoreCase(request.getAnswererName().trim()));
            }
            if (request.getIds() != null && !request.getIds().isEmpty()) {
                builder.and(criticalReport.id.in(request.getIds()));
            }
            if (request.getReporterIds() != null && !request.getReporterIds().isEmpty()) {
                builder.and(criticalReport.reporter.id.in(request.getReporterIds()));
            }
            if (request.getAnswererIds() != null && !request.getAnswererIds().isEmpty()) {
                builder.and(criticalReport.answerer.id.in(request.getAnswererIds()));
            }
            if (request.getResidentIds() != null && !request.getResidentIds().isEmpty()) {
                builder.and(criticalReport.resident.id.in(request.getResidentIds()));
            }
            if (request.getFeedbackIds() != null && !request.getFeedbackIds().isEmpty()) {
                builder.and(criticalReport.feedback.id.in(request.getFeedbackIds()));
            }
            if (request.getExchangeRequestIds() != null && !request.getExchangeRequestIds().isEmpty()) {
                builder.and(criticalReport.exchangeRequest.id.in(request.getExchangeRequestIds()));
            }
            if (request.getTypeReports() != null && !request.getTypeReports().isEmpty()) {
                builder.and(criticalReport.typeReport.in(request.getTypeReports()));
            }
            if (request.getStatusCriticalReports() != null && !request.getStatusCriticalReports().isEmpty()) {
                builder.and(criticalReport.statusCriticalReport.in(request.getStatusCriticalReports()));
            }
        } else {
            // if request null, fetch all report with status ACTIVE by default
            builder.and(criticalReport.statusEntity.eq(StatusEntity.ACTIVE));
        }
        return builder;
    }

    @Override
    protected OrderSpecifier<?> mapSortPropertyToOrderSpecifier(String property, Sort.Direction direction, QCriticalReport criticalReport) {
        switch (property) {
            case "id":
                return new OrderSpecifier<>(
                        direction.isAscending() ? Order.ASC : Order.DESC, criticalReport.id);
            case "typeReport":
                return new OrderSpecifier<>(
                        direction.isAscending() ? Order.ASC : Order.DESC, criticalReport.typeReport);
            case "statusCriticalReport":
                return new OrderSpecifier<>(
                        direction.isAscending() ? Order.ASC : Order.DESC, criticalReport.statusCriticalReport);
            case "resident":
                return new OrderSpecifier<>(
                        direction.isAscending() ? Order.ASC : Order.DESC, criticalReport.resident.fullName);
            case "feedback":
                return new OrderSpecifier<>(
                        direction.isAscending() ? Order.ASC : Order.DESC, criticalReport.feedback.id);
            case "exchangeRequest":
                return new OrderSpecifier<>(
                        direction.isAscending() ? Order.ASC : Order.DESC, criticalReport.exchangeRequest.id);
            case "reporter":
                return new OrderSpecifier<>(
                        direction.isAscending() ? Order.ASC : Order.DESC, criticalReport.reporter.fullName);
            case "answerer":
                return new OrderSpecifier<>(
                        direction.isAscending() ? Order.ASC : Order.DESC, criticalReport.answerer.fullName);
            case "statusEntity":
                return new OrderSpecifier<>(
                        direction.isAscending() ? Order.ASC : Order.DESC, criticalReport.statusEntity);
            case "resolvedTime":
                return new OrderSpecifier<>(
                        direction.isAscending() ? Order.ASC : Order.DESC, criticalReport.resolvedTime);
            default:
                logger.warn("Unknown sort property: " + property);
                return null;
        }
    }

    @Override
    public Page<CriticalReport> searchCriticalReportPagination(SearchCriticalReportRequest request, Pageable pageable) {
        return super.searchPagination(request, pageable);
    }

    @Override
    public CriticalReport findByExchange(ExchangeRequest exchangeRequest) {
        QCriticalReport criticalReport = getEntityPath();
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(criticalReport.exchangeRequest.eq(exchangeRequest))
                .and(criticalReport.statusCriticalReport.eq(StatusCriticalReport.PENDING));

        return new JPAQuery<CriticalReport>(em)
                .from(criticalReport)
                .where(builder)
                .fetchOne();
    }

    @Override
    public Optional<CriticalReport> findCriticalReportById(Integer criticalReportId) {
        QCriticalReport criticalReport = getEntityPath();

        return Optional.ofNullable(
                new JPAQuery<CriticalReport>(em)
                        .from(criticalReport)
                        .leftJoin(criticalReport.exchangeRequest).fetchJoin()
                        .leftJoin(criticalReport.resident).fetchJoin()
                        .leftJoin(criticalReport.feedback).fetchJoin()
                        .leftJoin(criticalReport.reporter).fetchJoin()
                        .leftJoin(criticalReport.answerer).fetchJoin()
                        .where(criticalReport.id.eq(criticalReportId))
                        .fetchOne()
        );
    }
}
