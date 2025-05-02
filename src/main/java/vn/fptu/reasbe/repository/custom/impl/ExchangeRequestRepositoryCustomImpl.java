package vn.fptu.reasbe.repository.custom.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import org.apache.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import vn.fptu.reasbe.model.entity.ExchangeRequest;
import vn.fptu.reasbe.model.entity.Item;
import vn.fptu.reasbe.model.entity.QExchangeRequest;
import vn.fptu.reasbe.model.entity.QExchangeHistory;
import vn.fptu.reasbe.model.entity.User;
import vn.fptu.reasbe.model.enums.exchange.StatusExchangeHistory;
import vn.fptu.reasbe.model.enums.exchange.StatusExchangeRequest;
import vn.fptu.reasbe.repository.custom.ExchangeRequestRepositoryCustom;
import vn.fptu.reasbe.repository.custom.core.AbstractRepositoryCustom;

import java.time.LocalDateTime;
import java.util.List;

public class ExchangeRequestRepositoryCustomImpl extends AbstractRepositoryCustom<ExchangeRequest, QExchangeRequest> implements ExchangeRequestRepositoryCustom {

    private static final Logger logger = Logger.getLogger(ExchangeRequestRepositoryCustomImpl.class);

    @Override
    protected QExchangeRequest getEntityPath() {
        return QExchangeRequest.exchangeRequest;
    }

    @Override
    protected <T> Expression<T> getIdPath(QExchangeRequest entityPath) {
        return (Expression<T>) entityPath.id;
    }

    @Override
    protected BooleanBuilder buildFilter(Object request, QExchangeRequest entityPath) {
        return new BooleanBuilder(); // Modify if needed for filtering logic
    }

    @Override
    protected OrderSpecifier<?> mapSortPropertyToOrderSpecifier(String property, Sort.Direction direction, QExchangeRequest entityPath) {
        switch (property) {
            case "id":
                return new OrderSpecifier<>(direction.isAscending() ? Order.ASC : Order.DESC, entityPath.id);
            case "creationDate":
                return new OrderSpecifier<>(direction.isAscending() ? Order.ASC : Order.DESC, entityPath.creationDate);
            case "estimatePrice":
                return new OrderSpecifier<>(direction.isAscending() ? Order.ASC : Order.DESC, entityPath.estimatePrice);
            case "finalPrice":
                return new OrderSpecifier<>(direction.isAscending() ? Order.ASC : Order.DESC, entityPath.finalPrice);
            case "exchangeDate":
                return new OrderSpecifier<>(direction.isAscending() ? Order.ASC : Order.DESC, entityPath.exchangeDate);
            default:
                logger.warn("Unknown sort property: " + property);
                return null;
        }
    }

    // ✅ Convert findByExchangeRequestStatusAndUser to QueryDSL
    @Override
    public Page<ExchangeRequest> findByExchangeRequestStatusAndUser(StatusExchangeRequest status, User user, Pageable pageable) {
        QExchangeRequest exchangeRequest = getEntityPath();

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(exchangeRequest.statusExchangeRequest.eq(status))
                .and(getFilterForSellerItemBuyerItemAndPaidBy(exchangeRequest, user));

        JPAQuery<ExchangeRequest> query = new JPAQuery<ExchangeRequest>(em)
                .from(getEntityPath())
                .where(builder);

        return getPage(query, pageable);
    }

    @Override
    public Page<ExchangeRequest> findByExchangeHistoryStatusAndUser(StatusExchangeHistory status, User user, Pageable pageable) {
        QExchangeRequest exchangeRequest = getEntityPath();

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(exchangeRequest.exchangeHistory.statusExchangeHistory.eq(status))
                .and(getFilterForSellerItemBuyerItemAndPaidBy(exchangeRequest, user));

        JPAQuery<ExchangeRequest> query = new JPAQuery<ExchangeRequest>(em)
                .from(getEntityPath())
                .where(builder);

        return getPage(query, pageable);
    }

    @Override
    public Page<ExchangeRequest> findByExchangeHistoryStatusInAndUser(List<StatusExchangeHistory> statuses, User user, Pageable pageable) {
        QExchangeRequest exchangeRequest = getEntityPath();

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(exchangeRequest.exchangeHistory.statusExchangeHistory.in(statuses))
                .and(getFilterForSellerItemBuyerItemAndPaidBy(exchangeRequest, user));

        JPAQuery<ExchangeRequest> query = new JPAQuery<ExchangeRequest>(em)
                .from(getEntityPath())
                .where(builder);

        return getPage(query, pageable);
    }

    @Override
    public List<ExchangeRequest> findAllExceedingDateExchanges(LocalDateTime date, StatusExchangeHistory status) {
        QExchangeRequest exchangeRequest = getEntityPath();
        QExchangeHistory exchangeHistory = QExchangeHistory.exchangeHistory;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(exchangeRequest.exchangeDate.before(date))
                .and(exchangeRequest.exchangeHistory.statusExchangeHistory.eq(status));

        return new JPAQuery<ExchangeRequest>(em)
                .from(exchangeRequest)
                .join(exchangeRequest.exchangeHistory, exchangeHistory)
                .where(builder)
                .fetch(); // Directly fetch results as a list
    }

    @Override
    public List<ExchangeRequest> findAllByStatusAndSellerItemOrBuyerItem(StatusExchangeRequest status, Item sellerItem, Item buyerItem) {
        QExchangeRequest exchangeRequest = getEntityPath();

        BooleanBuilder builder = new BooleanBuilder();
        BooleanBuilder filter = new BooleanBuilder();

        filter.or(exchangeRequest.sellerItem.eq(sellerItem))
                .or(exchangeRequest.buyerItem.eq(sellerItem));

        if (buyerItem != null) {
            filter.or(exchangeRequest.sellerItem.eq(buyerItem))
                    .or(exchangeRequest.buyerItem.eq(buyerItem));
        }

        builder.and(exchangeRequest.statusExchangeRequest.eq(status))
                .and(filter);

        return new JPAQuery<ExchangeRequest>(em)
                .from(exchangeRequest)
                .where(builder)
                .fetch();
    }

    @Override
    public boolean existByItemAndStatus(Integer itemId, StatusExchangeRequest status) {
        QExchangeRequest exchangeRequest = QExchangeRequest.exchangeRequest;

        BooleanBuilder builder = new BooleanBuilder();

        builder.and(exchangeRequest.statusExchangeRequest.eq(status))
                .and(exchangeRequest.sellerItem.id.eq(itemId)
                        .or(exchangeRequest.buyerItem.id.eq(itemId)));

        return new JPAQuery<ExchangeRequest>(em)
                .from(exchangeRequest)
                .where(builder)
                .fetchFirst() != null;
    }

    @Override
    public List<ExchangeRequest> findRelatedCancelledExchangeRequests(Item sellerItem, LocalDateTime cancelDateTime) {
        QExchangeRequest exchangeRequest = getEntityPath();
        BooleanBuilder builder = new BooleanBuilder();

        LocalDateTime upperBound = cancelDateTime.plusMinutes(5);

        builder.and(exchangeRequest.sellerItem.eq(sellerItem))
                .and(exchangeRequest.statusExchangeRequest.eq(StatusExchangeRequest.CANCELLED))
                .and(exchangeRequest.lastModificationDate.goe(cancelDateTime))
                .and(exchangeRequest.lastModificationDate.loe(upperBound));

        return new JPAQuery<ExchangeRequest>(em)
                .from(exchangeRequest)
                .where(builder)
                .fetch();
    }


    private BooleanBuilder getFilterForSellerItemBuyerItemAndPaidBy(QExchangeRequest exchangeRequest, User user) {
        BooleanBuilder builder = new BooleanBuilder();

        // Điều kiện khi user thuộc vào sellerItem, paidBy hoặc buyerItem.owner
        BooleanBuilder userConditions = new BooleanBuilder();
        userConditions.or(exchangeRequest.paidBy.eq(user))
                .or(exchangeRequest.sellerItem.owner.eq(user))
                .or(JPAExpressions.selectOne()
                        .from(exchangeRequest.buyerItem)
                        .where(exchangeRequest.buyerItem.owner.eq(user))
                        .exists());

        // Chỉ giữ lại buyerItem = NULL nếu user đã khớp với một trong các điều kiện trên
        builder.and(userConditions);
        builder.or(exchangeRequest.buyerItem.isNull().and(userConditions));

        return builder;
    }

    private Page<ExchangeRequest> getPage(JPAQuery<ExchangeRequest> query, Pageable pageable) {
        if (query == null || pageable == null) {
            throw new IllegalArgumentException("Query and pageable must not be null");
        }

        BooleanBuilder filters = new BooleanBuilder(query.getMetadata().getWhere());

        // Optimized count query using the same filter
        Long total = new JPAQuery<>(em)
                .select(getEntityPath().count())
                .from(getEntityPath())
                .where(filters)  // Use the same filters
                .fetchOne();

        long totalCount = (total != null) ? total : 0;

        if (totalCount == 0) {
            return Page.empty(pageable);
        }

        // Apply sorting
        applySorting(query, pageable);

        // Apply pagination and fetch results
        List<ExchangeRequest> results = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(results, pageable, totalCount);
    }

    // Apply sorting from Pageable
    private void applySorting(JPAQuery<ExchangeRequest> query, Pageable pageable) {
        Sort.Order sortOrder = pageable.getSort().iterator().next();
        String property = sortOrder.getProperty();
        Sort.Direction direction = sortOrder.getDirection();

        OrderSpecifier<?> orderSpecifier = mapSortPropertyToOrderSpecifier(property, direction, getEntityPath());
        if (orderSpecifier != null) {
            query.orderBy(orderSpecifier);
        } else {
            query.orderBy(getDefaultOrder(getEntityPath()));
        }
    }
}
