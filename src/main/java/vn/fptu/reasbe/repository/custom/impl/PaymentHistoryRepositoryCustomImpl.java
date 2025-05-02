package vn.fptu.reasbe.repository.custom.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import vn.fptu.reasbe.model.dto.paymenthistory.SearchPaymentHistoryRequest;
import vn.fptu.reasbe.model.entity.PaymentHistory;
import vn.fptu.reasbe.model.entity.QPaymentHistory;
import vn.fptu.reasbe.model.entity.QSubscriptionPlan;
import vn.fptu.reasbe.model.entity.QUserSubscription;
import vn.fptu.reasbe.model.enums.core.StatusEntity;
import vn.fptu.reasbe.model.enums.payment.StatusPayment;
import vn.fptu.reasbe.model.enums.subscriptionplan.TypeSubscriptionPlan;
import vn.fptu.reasbe.repository.custom.PaymentHistoryRepositoryCustom;
import vn.fptu.reasbe.repository.custom.core.AbstractRepositoryCustom;
import vn.fptu.reasbe.utils.common.DateUtils;

/**
 *
 * @author dungnguyen
 */
public class PaymentHistoryRepositoryCustomImpl extends AbstractRepositoryCustom<PaymentHistory, QPaymentHistory> implements PaymentHistoryRepositoryCustom {

    private static final Logger logger = Logger.getLogger(PaymentHistoryRepositoryCustomImpl.class);

    @PersistenceContext
    protected EntityManager em;

    @Override
    protected QPaymentHistory getEntityPath() {
        return QPaymentHistory.paymentHistory;
    }

    @Override
    protected <T> Expression<T> getIdPath(QPaymentHistory entityPath) {
        return (Expression<T>) entityPath.id;
    }

    @Override
    public Page<PaymentHistory> searchPaymentHistoryPagination(SearchPaymentHistoryRequest request, Pageable pageable) {
        return super.searchPagination(request, pageable);
    }

    @Override
    public BigDecimal getMonthlyRevenue(Integer month, Integer year) {
        QPaymentHistory paymentHistory = getEntityPath();

        NumberExpression<Integer> monthExpr = DateUtils.extractMonth(paymentHistory.transactionDateTime);
        NumberExpression<Integer> yearExpr = DateUtils.extractYear(paymentHistory.transactionDateTime);

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(monthExpr.eq(month))
                .and(yearExpr.eq(year))
                .and(paymentHistory.statusEntity.eq(StatusEntity.ACTIVE))
                .and(paymentHistory.statusPayment.eq(StatusPayment.SUCCESS));

        BigDecimal revenue = new JPAQuery<>(em).from(paymentHistory)
                .select(paymentHistory.amount.sum())
                .where(builder)
                .fetchOne();

        return revenue == null ? BigDecimal.ZERO : revenue;
    }

    @Override
    public Map<TypeSubscriptionPlan, BigDecimal> getMonthlyRevenueBySubscriptionPlan(Integer month, Integer year) {
        QPaymentHistory paymentHistory = QPaymentHistory.paymentHistory;
        QUserSubscription userSubscription = QUserSubscription.userSubscription;
        QSubscriptionPlan subscriptionPlan = QSubscriptionPlan.subscriptionPlan;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(DateUtils.extractMonth(paymentHistory.transactionDateTime).eq(month))
                .and(DateUtils.extractYear(paymentHistory.transactionDateTime).eq(year))
                .and(paymentHistory.statusEntity.eq(StatusEntity.ACTIVE))
                .and(paymentHistory.statusPayment.eq(StatusPayment.SUCCESS));

        List<Tuple> results = new JPAQuery<>(em)
                .from(paymentHistory)
                .leftJoin(paymentHistory.userSubscription, userSubscription)
                .leftJoin(userSubscription.subscriptionPlan, subscriptionPlan)
                .where(builder)
                .groupBy(subscriptionPlan.typeSubscriptionPlan)
                .select(subscriptionPlan.typeSubscriptionPlan, paymentHistory.amount.sum())
                .fetch();

        Map<TypeSubscriptionPlan, BigDecimal> revenueMap = new HashMap<>();
        for (Tuple tuple : results) {
            TypeSubscriptionPlan type = tuple.get(subscriptionPlan.typeSubscriptionPlan);
            BigDecimal revenue = tuple.get(paymentHistory.amount.sum());
            revenueMap.put(type, revenue);
        }
        return revenueMap;
    }

    @Override
    public HashMap<Integer, Map<TypeSubscriptionPlan, BigDecimal>> getMonthlyRevenueBySubscriptionPlanInAYear(Integer year) {
        QPaymentHistory paymentHistory = QPaymentHistory.paymentHistory;
        QUserSubscription userSubscription = QUserSubscription.userSubscription;
        QSubscriptionPlan subscriptionPlan = QSubscriptionPlan.subscriptionPlan;

        NumberExpression<Integer> monthExpr = DateUtils.extractMonth(paymentHistory.transactionDateTime);
        NumberExpression<Integer> yearExpr = DateUtils.extractYear(paymentHistory.transactionDateTime);

        BooleanBuilder builder = new BooleanBuilder()
                .and(yearExpr.eq(year))
                .and(paymentHistory.statusEntity.eq(StatusEntity.ACTIVE))
                .and(paymentHistory.statusPayment.eq(StatusPayment.SUCCESS));

        List<Tuple> results = new JPAQuery<>(em)
                .from(paymentHistory)
                .leftJoin(paymentHistory.userSubscription, userSubscription)
                .leftJoin(userSubscription.subscriptionPlan, subscriptionPlan)
                .where(builder)
                .groupBy(monthExpr, subscriptionPlan.typeSubscriptionPlan)
                .select(monthExpr, subscriptionPlan.typeSubscriptionPlan, paymentHistory.amount.sum())
                .fetch();

        HashMap<Integer, Map<TypeSubscriptionPlan, BigDecimal>> revenueByMonth = new HashMap<>();

        for (Tuple tuple : results) {
            Integer monthValue = tuple.get(monthExpr);
            TypeSubscriptionPlan planType = tuple.get(subscriptionPlan.typeSubscriptionPlan);
            BigDecimal sumAmount = tuple.get(paymentHistory.amount.sum());

            // Get or create the inner map for that month
            Map<TypeSubscriptionPlan, BigDecimal> planRevenueMap =
                    revenueByMonth.computeIfAbsent(monthValue, k -> new HashMap<>());

            planRevenueMap.put(planType, sumAmount != null ? sumAmount : BigDecimal.ZERO);
        }

        return revenueByMonth;
    }

    @Override
    protected BooleanBuilder buildFilter(Object requestObj, QPaymentHistory entityPath) {
        QPaymentHistory paymentHistory = getEntityPath();
        BooleanBuilder builder = new BooleanBuilder();
        SearchPaymentHistoryRequest request = (SearchPaymentHistoryRequest) requestObj;

        if (request != null) {
            if (request.getUserId() != null) {
                builder.and(paymentHistory.userSubscription.user.id.eq(request.getUserId()));
            }
            if (request.getTransactionId() != null) {
                String transactionIdStr = String.valueOf(request.getTransactionId());
                builder.and(paymentHistory.transactionId.stringValue().like("%" + transactionIdStr + "%"));
            }
            if (request.getPrice() != null) {
                builder.and(paymentHistory.amount.eq(request.getPrice()));
            }
            if (request.getFromPrice() != null && request.getToPrice() != null) {
                builder.and(paymentHistory.amount.between(request.getFromPrice(), request.getToPrice()));
            } else if (request.getFromPrice() == null && request.getToPrice() != null) {
                builder.and(paymentHistory.amount.loe(request.getToPrice()));
            } else if (request.getFromPrice() != null && request.getToPrice() == null) {
                builder.and(paymentHistory.amount.goe(request.getFromPrice()));
            }
            if (request.getStatusPayments() != null && !request.getStatusPayments().isEmpty()) {
                builder.and(paymentHistory.statusPayment.in(request.getStatusPayments()));
            }
            if (request.getMethodPayments() != null && !request.getMethodPayments().isEmpty()) {
                builder.and(paymentHistory.methodPayment.in(request.getMethodPayments()));
            }
            if (request.getStatusEntities() != null && !request.getStatusEntities().isEmpty()) {
                builder.and(paymentHistory.statusEntity.in(request.getStatusEntities()));
            }
            if (request.getFromTransactionDate() != null && request.getToTransactionDate() != null) {
                builder.and(paymentHistory.transactionDateTime.between(request.getFromTransactionDate(), request.getToTransactionDate()));
            } else if (request.getFromTransactionDate() == null && request.getToTransactionDate() != null) {
                builder.and(paymentHistory.transactionDateTime.loe(request.getToTransactionDate()));
            } else if (request.getFromTransactionDate() != null && request.getToTransactionDate() == null) {
                builder.and(paymentHistory.transactionDateTime.goe(request.getFromTransactionDate()));
            }
        } else {
            builder.and(paymentHistory.statusEntity.eq(StatusEntity.ACTIVE));
        }
        return builder;
    }

    @Override
    protected OrderSpecifier<?> mapSortPropertyToOrderSpecifier(String property, Sort.Direction direction, QPaymentHistory paymentHistory) {
        switch (property) {
            case "transactionId":
                return new OrderSpecifier<>(direction.isAscending() ? Order.ASC : Order.DESC, paymentHistory.transactionId);
            case "amount":
                return new OrderSpecifier<>(direction.isAscending() ? Order.ASC : Order.DESC, paymentHistory.amount);
            case "transactionDateTime":
                return new OrderSpecifier<>(direction.isAscending() ? Order.ASC : Order.DESC, paymentHistory.transactionDateTime);
            case "description":
                return new OrderSpecifier<>(direction.isAscending() ? Order.ASC : Order.DESC, paymentHistory.description);
            case "statusPayment":
                return new OrderSpecifier<>(direction.isAscending() ? Order.ASC : Order.DESC, paymentHistory.statusPayment);
            case "methodPayment":
                return new OrderSpecifier<>(direction.isAscending() ? Order.ASC : Order.DESC, paymentHistory.methodPayment);
            default:
                logger.warn("Invalid sort property: " + property);
                return null;
        }
    }
}
