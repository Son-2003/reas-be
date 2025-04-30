package vn.fptu.reasbe.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.fptu.reasbe.model.entity.User;
import vn.fptu.reasbe.model.entity.UserSubscription;
import vn.fptu.reasbe.model.enums.core.StatusEntity;
import vn.fptu.reasbe.model.enums.payment.StatusPayment;
import vn.fptu.reasbe.model.enums.subscriptionplan.TypeSubscriptionPlan;

/**
 *
 * @author dungnguyen
 */
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {
    UserSubscription findByUserIdAndSubscriptionPlan_TypeSubscriptionPlanAndEndDateIsAfterAndPaymentHistory_StatusPaymentAndStatusEntity(
            Integer userId, TypeSubscriptionPlan typeSubscriptionPlan, LocalDateTime currentTime, StatusPayment statusPayment, StatusEntity statusEntity);

    boolean existsByUserAndSubscriptionPlan_TypeSubscriptionPlanAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            User user,
            TypeSubscriptionPlan typeSubscriptionPlan,
            LocalDateTime currentDate1,
            LocalDateTime currentDate2
    );

    UserSubscription findByUserIdAndSubscriptionPlan_TypeSubscriptionPlanAndEndDateIsBetweenAndPaymentHistory_StatusPaymentAndStatusEntity(
            Integer userId, TypeSubscriptionPlan typeSubscriptionPlan, LocalDateTime startOfCurrentMonth, LocalDateTime currentTime, StatusPayment statusPayment, StatusEntity statusEntity);
}
