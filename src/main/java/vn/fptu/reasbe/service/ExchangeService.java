package vn.fptu.reasbe.service;

import vn.fptu.reasbe.model.dto.core.BaseSearchPaginationResponse;
import vn.fptu.reasbe.model.dto.exchange.EvidenceExchangeRequest;
import vn.fptu.reasbe.model.dto.exchange.ExchangeRequestRequest;
import vn.fptu.reasbe.model.dto.exchange.ExchangeResponse;
import vn.fptu.reasbe.model.enums.exchange.StatusExchangeHistory;
import vn.fptu.reasbe.model.enums.exchange.StatusExchangeRequest;

import java.math.BigDecimal;

public interface ExchangeService {
    BaseSearchPaginationResponse<ExchangeResponse> getAllExchangeByStatusOfCurrentUser(int pageNo, int pageSize, String sortBy, String sortDir, StatusExchangeRequest statusRequest, StatusExchangeHistory statusHistory);

    BaseSearchPaginationResponse<ExchangeResponse> getAllExchangeHistoryOfUser(int pageNo, int pageSize, String sortBy, String sortDir, Integer userId);

    ExchangeResponse getExchangeById(Integer id);

    ExchangeResponse createExchangeRequest(ExchangeRequestRequest exchangeRequestRequest);

    ExchangeResponse updateExchangeRequestPrice(Integer id, BigDecimal negotiatedPrice);

    ExchangeResponse reviewExchangeRequest(Integer id, StatusExchangeRequest statusExchangeRequest);

    ExchangeResponse cancelExchange(Integer id);

    ExchangeResponse confirmNegotiatedPrice(Integer id);

    ExchangeResponse uploadEvidence(EvidenceExchangeRequest request);

    Integer getNumberOfSuccessfulExchanges(Integer month, Integer year);

    Integer getNumberOfSuccessfulExchangesOfUser(Integer month, Integer year);
}