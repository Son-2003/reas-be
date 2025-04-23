package vn.fptu.reasbe.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.apache.commons.lang3.tuple.Triple;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import vn.fptu.reasbe.model.dto.core.BaseSearchPaginationResponse;
import vn.fptu.reasbe.model.dto.paymenthistory.PaymentHistoryDto;
import vn.fptu.reasbe.model.dto.paymenthistory.SearchPaymentHistoryRequest;
import vn.fptu.reasbe.model.entity.Item;
import vn.fptu.reasbe.model.entity.PaymentHistory;
import vn.fptu.reasbe.model.entity.SubscriptionPlan;
import vn.fptu.reasbe.model.entity.User;
import vn.fptu.reasbe.model.enums.core.StatusEntity;
import vn.fptu.reasbe.model.enums.payment.MethodPayment;
import vn.fptu.reasbe.model.enums.payment.StatusPayment;
import vn.fptu.reasbe.model.enums.subscriptionplan.TypeSubscriptionPlan;
import vn.fptu.reasbe.model.enums.user.RoleName;
import vn.fptu.reasbe.model.exception.PayOSException;
import vn.fptu.reasbe.model.exception.ReasApiException;
import vn.fptu.reasbe.model.exception.ResourceNotFoundException;
import vn.fptu.reasbe.repository.PaymentHistoryRepository;
import vn.fptu.reasbe.service.AuthService;
import vn.fptu.reasbe.service.ItemService;
import vn.fptu.reasbe.service.PaymentHistoryService;
import vn.fptu.reasbe.service.SubscriptionPlanService;
import vn.fptu.reasbe.service.UserService;
import vn.fptu.reasbe.service.UserSubscriptionService;
import vn.fptu.reasbe.utils.common.DateUtils;
import vn.fptu.reasbe.utils.common.PaymentCodeHelper;
import vn.fptu.reasbe.utils.mapper.PaymentHistoryMapper;
import vn.payos.PayOS;
import vn.payos.type.Webhook;
import vn.payos.type.WebhookData;

/**
 *
 * @author dungnguyen
 */
@Service
@Transactional
@RequiredArgsConstructor
public class PaymentHistoryServiceImpl implements PaymentHistoryService {

    private final PayOS payOS;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final UserSubscriptionService userSubscriptionService;
    private final PaymentHistoryMapper paymentHistoryMapper;
    private final SubscriptionPlanService subscriptionPlanService;
    private final UserService userService;
    private final ItemService itemService;
    private final AuthService authService;

    @Override
    public BaseSearchPaginationResponse<PaymentHistoryDto> searchPaymentHistoryPagination(int pageNo, int pageSize, String sortBy, String sortDir, SearchPaymentHistoryRequest request) {
        return BaseSearchPaginationResponse.of(paymentHistoryRepository.searchPaymentHistoryPagination(request, BaseSearchPaginationResponse.getPageable(pageNo, pageSize, sortBy, sortDir))
                .map(paymentHistoryMapper::toDto));
    }

    @Override
    public BaseSearchPaginationResponse<PaymentHistoryDto> searchPaymentHistoryOfUserPagination(int pageNo, int pageSize, String sortBy, String sortDir, SearchPaymentHistoryRequest request, Integer userId) {
        User currentUser = authService.getCurrentUser();

        if (currentUser.getRole().getName().equals(RoleName.ROLE_RESIDENT) && !currentUser.getId().equals(userId)) {
            throw new ReasApiException(HttpStatus.FORBIDDEN, "error.userNotAllowed");
        }

        if (userService.getUserById(userId) != null) {
            if (request == null) {
                request = new SearchPaymentHistoryRequest();
            }
            request.setUserId(userId);
            return BaseSearchPaginationResponse.of(paymentHistoryRepository.searchPaymentHistoryPagination(request, BaseSearchPaginationResponse.getPageable(pageNo, pageSize, sortBy, sortDir))
                    .map(paymentHistoryMapper::toDto));
        }
        return null;
    }

    @Override
    public void payOsTransferHandler(ObjectNode body) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Webhook webhookBody = objectMapper.treeToValue(body, Webhook.class);
            WebhookData data = payOS.verifyPaymentWebhookData(webhookBody);
            SubscriptionPlan subscriptionPlan;
            User user;
            Item item = null;

            if (data.getOrderCode() != 123) {
                Triple<Integer, Integer, Integer> orderCodeInfo = PaymentCodeHelper.getInfoFromOrderCode(data.getOrderCode());

                Integer subscriptionPlanId = Math.toIntExact(orderCodeInfo.getLeft());
                subscriptionPlan = subscriptionPlanService.getSubscriptionPlanByPlanId(subscriptionPlanId);

                Integer userId = Math.toIntExact(orderCodeInfo.getMiddle());
                user = userService.getUserById(userId);

                if (subscriptionPlan.getTypeSubscriptionPlan().equals(TypeSubscriptionPlan.ITEM_EXTENSION)) {
                    int itemId = Math.toIntExact(orderCodeInfo.getRight());
                    if (itemId != 0) {
                        item = itemService.getItemById(itemId);
                    }
                }
            } else {
                subscriptionPlan = subscriptionPlanService.getSubscriptionPlanByPlanId(1);
                user = null;
            }

            PaymentHistory paymentHistory = PaymentHistory.builder()
                    .transactionId(data.getOrderCode())
                    .amount(BigDecimal.valueOf(data.getAmount()))
                    .description(data.getDescription())
                    .transactionDateTime(LocalDateTime.parse(data.getTransactionDateTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .statusPayment(Boolean.TRUE.equals(webhookBody.getSuccess()) ? StatusPayment.SUCCESS : StatusPayment.FAILED)
                    .methodPayment(MethodPayment.BANK_TRANSFER)
                    .build();
            PaymentHistory savedPaymentHistory = paymentHistoryRepository.save(paymentHistory);

            if (Boolean.TRUE.equals(webhookBody.getSuccess())) {
                userSubscriptionService.createUserSubscription(subscriptionPlan, user, item, savedPaymentHistory);
                if (item != null) {
                    itemService.extendItem(item, subscriptionPlan);
                }
            }
        } catch (Exception e) {
            throw new PayOSException(e.getMessage());
        }
    }

    @Override
    public PaymentHistory getPaymentHistoryById(Integer id) {
        return paymentHistoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("PaymentHistory", "id", id));
    }

    @Override
    public BigDecimal getMonthlyRevenue(Integer month, Integer year) {
        return paymentHistoryRepository.getMonthlyRevenue(month, year);
    }

    @Override
    public Integer getNumberOfSuccessfulTransaction(Integer month, Integer year) {
        LocalDateTime startDate = DateUtils.getStartOfSpecificMonth(month, year);
        LocalDateTime endDate = DateUtils.getEndOfSpecificMonth(month, year);
        return paymentHistoryRepository.countByTransactionDateTimeIsBetweenAndStatusPaymentAndStatusEntity(startDate, endDate, StatusPayment.SUCCESS, StatusEntity.ACTIVE);
    }

    @Override
    public Integer getNumberOfSuccessfulTransactionOfUser(Integer month, Integer year) {
        User user = authService.getCurrentUser();
        LocalDateTime startDate = DateUtils.getStartOfSpecificMonth(month, year);
        LocalDateTime endDate = DateUtils.getEndOfSpecificMonth(month, year);
        return paymentHistoryRepository.countByTransactionDateTimeIsBetweenAndStatusPaymentAndUserSubscription_User_IdAndStatusEntity(startDate, endDate, StatusPayment.SUCCESS, user.getId(), StatusEntity.ACTIVE);
    }

    @Override
    public Map<TypeSubscriptionPlan, BigDecimal> getMonthlyRevenueBySubscriptionPlan(Integer month, Integer year) {
        return paymentHistoryRepository.getMonthlyRevenueBySubscriptionPlan(month, year);
    }

    @Override
    public Map<Integer, Map<TypeSubscriptionPlan, BigDecimal>> getMonthlyRevenueBySubscriptionPlanInAYear(Integer year) {
        return paymentHistoryRepository.getMonthlyRevenueBySubscriptionPlanInAYear(year);
    }
}
