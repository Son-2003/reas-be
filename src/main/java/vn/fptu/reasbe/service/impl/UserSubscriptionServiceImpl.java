package vn.fptu.reasbe.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import vn.fptu.reasbe.model.entity.Item;
import vn.fptu.reasbe.model.entity.PaymentHistory;
import vn.fptu.reasbe.model.entity.SubscriptionPlan;
import vn.fptu.reasbe.model.entity.User;
import vn.fptu.reasbe.model.entity.UserSubscription;
import vn.fptu.reasbe.model.enums.core.StatusEntity;
import vn.fptu.reasbe.model.enums.payment.StatusPayment;
import vn.fptu.reasbe.model.enums.subscriptionplan.TypeSubscriptionPlan;
import vn.fptu.reasbe.model.exception.ReasApiException;
import vn.fptu.reasbe.repository.UserSubscriptionRepository;
import vn.fptu.reasbe.service.AuthService;
import vn.fptu.reasbe.service.UserSubscriptionService;
import vn.fptu.reasbe.utils.common.DateUtils;
import vn.fptu.reasbe.utils.mapper.UserSubscriptionMapper;

import java.time.LocalDateTime;

/**
 *
 * @author dungnguyen
 */
@Service
@Transactional
@RequiredArgsConstructor
public class UserSubscriptionServiceImpl implements UserSubscriptionService {

    private final UserSubscriptionRepository userSubscriptionRepository;
    private final AuthService authService;
    private final UserSubscriptionMapper userSubscriptionMapper;

    @Override
    public void createUserSubscription(SubscriptionPlan subscriptionPlan, User user, Item item, PaymentHistory paymentHistory) {
        Float duration = subscriptionPlan.getDuration();

        LocalDateTime startDate = paymentHistory.getTransactionDateTime();

        LocalDateTime endDate = DateUtils.getEndDateByStartDateAndDuration(startDate, duration);

        UserSubscription userSubscription = UserSubscription.builder()
                .user(user)
                .subscriptionPlan(subscriptionPlan)
                .item(item)
                .paymentHistory(paymentHistory)
                .startDate(startDate)
                .endDate(endDate)
                .numberOfFreeExtensionLeft(subscriptionPlan.getNumberOfFreeExtension())
                .build();

        userSubscriptionRepository.save(userSubscription);
    }

    @Override
    public UserSubscription getUserCurrentSubscription() {
        User currentUser = authService.getCurrentUser();
        return userSubscriptionRepository.findByUserIdAndSubscriptionPlan_TypeSubscriptionPlanAndEndDateIsAfterAndPaymentHistory_StatusPaymentAndStatusEntity(
                currentUser.getId(),
                TypeSubscriptionPlan.PREMIUM_PLAN,
                DateUtils.getCurrentDateTime(),
                StatusPayment.SUCCESS,
                StatusEntity.ACTIVE
        );
    }

    @Override
    public void updateNumberOfExtensionLeft(UserSubscription userSubscription) {
        if (userSubscription.getNumberOfFreeExtensionLeft() > 0) {
            userSubscription.setNumberOfFreeExtensionLeft(userSubscription.getNumberOfFreeExtensionLeft() - 1);
            userSubscriptionRepository.save(userSubscription);
        } else {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.noExtensionLeft");
        }
    }

    @Override
    public UserSubscription getUserSubscriptionInCurrentMonth() {
        User currentUser = authService.getCurrentUser();
        return userSubscriptionRepository.findByUserIdAndSubscriptionPlan_TypeSubscriptionPlanAndEndDateIsBetweenAndPaymentHistory_StatusPaymentAndStatusEntity(
                currentUser.getId(),
                TypeSubscriptionPlan.PREMIUM_PLAN,
                DateUtils.getFirstDayOfCurrentMonth(),
                DateUtils.getCurrentDateTime(),
                StatusPayment.SUCCESS,
                StatusEntity.ACTIVE
        );
    }
}
