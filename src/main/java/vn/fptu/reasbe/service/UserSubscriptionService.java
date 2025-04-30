package vn.fptu.reasbe.service;

import vn.fptu.reasbe.model.entity.Item;
import vn.fptu.reasbe.model.entity.PaymentHistory;
import vn.fptu.reasbe.model.entity.SubscriptionPlan;
import vn.fptu.reasbe.model.entity.User;
import vn.fptu.reasbe.model.entity.UserSubscription;

/**
 *
 * @author dungnguyen
 */
public interface UserSubscriptionService {
    void createUserSubscription(SubscriptionPlan plan, User user, Item item, PaymentHistory paymentHistory);

    UserSubscription getUserCurrentSubscription();

    void updateNumberOfExtensionLeft(UserSubscription userSubscription);

    UserSubscription getUserSubscriptionInCurrentMonth();
}
