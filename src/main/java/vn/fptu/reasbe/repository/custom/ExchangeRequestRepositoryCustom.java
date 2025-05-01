package vn.fptu.reasbe.repository.custom;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.fptu.reasbe.model.entity.ExchangeRequest;
import vn.fptu.reasbe.model.entity.Item;
import vn.fptu.reasbe.model.entity.User;
import vn.fptu.reasbe.model.enums.exchange.StatusExchangeHistory;
import vn.fptu.reasbe.model.enums.exchange.StatusExchangeRequest;

import java.time.LocalDateTime;
import java.util.List;

public interface ExchangeRequestRepositoryCustom{
    Page<ExchangeRequest> findByExchangeRequestStatusAndUser(StatusExchangeRequest status, User user, Pageable pageable);

    Page<ExchangeRequest> findByExchangeHistoryStatusAndUser(StatusExchangeHistory status, User user, Pageable pageable);

    Page<ExchangeRequest> findByExchangeHistoryStatusInAndUser(List<StatusExchangeHistory> statuses, User user, Pageable pageable);

    List<ExchangeRequest> findAllExceedingDateExchanges(LocalDateTime date, StatusExchangeHistory status);

    List<ExchangeRequest> findAllByStatusAndSellerItemOrBuyerItem(StatusExchangeRequest statusExchangeRequest, Item sellerItem, Item buyerItem);

    boolean existByItemAndStatus(Item item, StatusExchangeRequest statusExchangeRequest);
}
