package vn.fptu.reasbe.controller;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import vn.fptu.reasbe.model.constant.AppConstants;
import vn.fptu.reasbe.model.dto.core.BaseSearchPaginationResponse;
import vn.fptu.reasbe.model.dto.paymenthistory.PaymentHistoryDto;
import vn.fptu.reasbe.model.dto.paymenthistory.SearchPaymentHistoryRequest;
import vn.fptu.reasbe.model.enums.subscriptionplan.TypeSubscriptionPlan;
import vn.fptu.reasbe.service.PaymentHistoryService;

/**
 *
 * @author dung
 */
@RestController
@RequestMapping("/api/v1/payment-history")
@RequiredArgsConstructor
public class PaymentHistoryController {

    private final PaymentHistoryService paymentHistoryService;

    @PostMapping("/search")
    @PreAuthorize("hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_STAFF) or " +
            "hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_ADMIN)")
    public ResponseEntity<BaseSearchPaginationResponse<PaymentHistoryDto>> searchPaymentHistoryPagination(
            @RequestParam(name = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(name = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDir,
            @RequestBody @Nullable SearchPaymentHistoryRequest request
    ) {
        return ResponseEntity.ok(paymentHistoryService.searchPaymentHistoryPagination(pageNo, pageSize, sortBy, sortDir, request));
    }

    @PostMapping("/search/{userId}")
    @PreAuthorize("hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_STAFF) " +
            "or hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_ADMIN)" +
            "or hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_RESIDENT)")
    public ResponseEntity<BaseSearchPaginationResponse<PaymentHistoryDto>> searchPaymentHistoryOfUserPagination(
            @RequestParam(name = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(name = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDir,
            @RequestBody @Nullable SearchPaymentHistoryRequest request,
            @PathVariable(name = "userId") Integer userId
    ) {
        return ResponseEntity.ok(paymentHistoryService.searchPaymentHistoryOfUserPagination(pageNo, pageSize, sortBy, sortDir, request, userId));
    }

    @PostMapping(value = "/payos-transfer-handler")
    public void payosTransferHandler(@RequestBody ObjectNode body) throws Exception {
        paymentHistoryService.payOsTransferHandler(body);
    }

    @GetMapping(value = "/monthly-revenue")
    @PreAuthorize("hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_STAFF) or " +
            "hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_ADMIN)")
    public ResponseEntity<BigDecimal> getMonthlyRevenue(@RequestParam Integer month, @RequestParam Integer year) {
        return ResponseEntity.ok(paymentHistoryService.getMonthlyRevenue(month, year));
    }

    @GetMapping(value = "/number-of-successful-transaction")
    @PreAuthorize("hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_STAFF) or " +
            "hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_ADMIN)")
    public ResponseEntity<Integer> getNumberOfSuccessfulTransaction(@RequestParam Integer month, @RequestParam Integer year) {
        return ResponseEntity.ok(paymentHistoryService.getNumberOfSuccessfulTransaction(month, year));
    }

    @GetMapping(value = "/number-of-successful-transaction-of-user")
    @PreAuthorize("hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_RESIDENT)")
    public ResponseEntity<Integer> getNumberOfSuccessfulTransactionOfUser(@RequestParam Integer month, @RequestParam Integer year) {
        return ResponseEntity.ok(paymentHistoryService.getNumberOfSuccessfulTransactionOfUser(month, year));
    }

    @GetMapping(value = "/monthly-revenue-by-subscription-plan")
    @PreAuthorize("hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_STAFF) or " +
            "hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_ADMIN)")
    public ResponseEntity<Map<TypeSubscriptionPlan, BigDecimal>> getMonthlyRevenueBySubscriptionPlan(@RequestParam Integer month, @RequestParam Integer year) {
        return ResponseEntity.ok(paymentHistoryService.getMonthlyRevenueBySubscriptionPlan(month, year));
    }

    @GetMapping(value = "/monthly-revenue-by-subscription-plan-in-a-year")
    @PreAuthorize("hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_STAFF) or " +
            "hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_ADMIN)")
    public ResponseEntity<Map<Integer, Map<TypeSubscriptionPlan, BigDecimal>>> getMonthlyRevenueBySubscriptionPlanInAYear(@RequestParam Integer year) {
        return ResponseEntity.ok(paymentHistoryService.getMonthlyRevenueBySubscriptionPlanInAYear(year));
    }
}
