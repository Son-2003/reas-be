package vn.fptu.reasbe.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
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
import vn.fptu.reasbe.model.dto.exchange.EvidenceExchangeRequest;
import vn.fptu.reasbe.model.dto.exchange.ExchangeRequestRequest;
import vn.fptu.reasbe.model.dto.exchange.ExchangeResponse;
import vn.fptu.reasbe.model.enums.exchange.StatusExchangeHistory;
import vn.fptu.reasbe.model.enums.exchange.StatusExchangeRequest;
import vn.fptu.reasbe.service.ExchangeService;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/exchange")
@RequiredArgsConstructor
public class ExchangeController {
    private final ExchangeService exchangeService;

    @GetMapping("/current-user")
    @PreAuthorize("hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_RESIDENT)")
    public ResponseEntity<BaseSearchPaginationResponse<ExchangeResponse>> getAllExchangesByStatusOfCurrentUser(
            @RequestParam(name = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(name = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDir,
            @RequestParam StatusExchangeRequest statusExchangeRequest,
            @RequestParam(required = false) StatusExchangeHistory statusExchangeHistory
    ) {
        return ResponseEntity.ok(exchangeService.getAllExchangeByStatusOfCurrentUser(pageNo, pageSize, sortBy, sortDir, statusExchangeRequest, statusExchangeHistory));
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_STAFF) or " +
            "hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_ADMIN)")
    public ResponseEntity<BaseSearchPaginationResponse<ExchangeResponse>> getAllExchangeHistoryOfUser(
            @RequestParam(name = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(name = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDir,
            @RequestParam Integer userId
    ) {
        return ResponseEntity.ok(exchangeService.getAllExchangeHistoryOfUser(pageNo, pageSize, sortBy, sortDir, userId));
    }

    @GetMapping("/{exchangeId}")
    @PreAuthorize("hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_RESIDENT) or " +
            "hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_STAFF) or " +
            "hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_ADMIN)")
    public ResponseEntity<ExchangeResponse> getExchangeDetail(@PathVariable Integer exchangeId) {
        return ResponseEntity.ok(exchangeService.getExchangeById(exchangeId));
    }

    @PostMapping
    @PreAuthorize("hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_RESIDENT)")
    public ResponseEntity<ExchangeResponse> makeAnExchange(@RequestBody @Valid ExchangeRequestRequest request) {
        return ResponseEntity.ok(exchangeService.createExchangeRequest(request));
    }

    @PutMapping("/negotiated-price")
    @PreAuthorize("hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_RESIDENT)")
    public ResponseEntity<ExchangeResponse> updateExchangeRequestPrice(@RequestParam Integer exchangeId, @RequestParam BigDecimal negotiatedPrice) {
        return ResponseEntity.ok(exchangeService.updateExchangeRequestPrice(exchangeId, negotiatedPrice));
    }

    @PutMapping("/negotiated-price/confirm")
    @PreAuthorize("hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_RESIDENT)")
    public ResponseEntity<ExchangeResponse> confirmNegotiatedPrice(@RequestParam Integer exchangeId) {
        return ResponseEntity.ok(exchangeService.confirmNegotiatedPrice(exchangeId));
    }

    @PutMapping("/review")
    @PreAuthorize("hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_RESIDENT)")
    public ResponseEntity<ExchangeResponse> reviewExchangeRequest(@RequestParam Integer exchangeId, @RequestParam StatusExchangeRequest statusExchangeRequest) {
        return ResponseEntity.ok(exchangeService.reviewExchangeRequest(exchangeId, statusExchangeRequest));
    }

    @PostMapping("/evidence")
    @PreAuthorize("hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_RESIDENT)")
    public ResponseEntity<ExchangeResponse> uploadExchangeEvidence(@RequestBody @Valid EvidenceExchangeRequest request) {
        return ResponseEntity.ok(exchangeService.uploadEvidence(request));
    }

    @PutMapping("/cancel")
    @PreAuthorize("hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_RESIDENT)")
    public ResponseEntity<ExchangeResponse> cancelExchange(@RequestParam Integer exchangeId) {
        return ResponseEntity.ok(exchangeService.cancelExchange(exchangeId));
    }

    @GetMapping("/number-of-successful-exchanges")
    @PreAuthorize("hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_STAFF) or " +
            "hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_ADMIN)")
    public ResponseEntity<Integer> getNumberOfSuccessfulExchanges(@RequestParam Integer month, @RequestParam Integer year) {
        return ResponseEntity.ok(exchangeService.getNumberOfSuccessfulExchanges(month, year));
    }

    @GetMapping("/number-of-successful-exchanges-of-user")
    @PreAuthorize("hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_RESIDENT)")
    public ResponseEntity<Integer> getNumberOfSuccessfulExchangesOfUser(@RequestParam Integer month, @RequestParam Integer year) {
        return ResponseEntity.ok(exchangeService.getNumberOfSuccessfulExchangesOfUser(month, year));
    }
}
