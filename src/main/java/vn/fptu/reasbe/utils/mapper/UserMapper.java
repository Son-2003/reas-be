package vn.fptu.reasbe.utils.mapper;

import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.springframework.stereotype.Component;
import vn.fptu.reasbe.model.dto.user.CreateStaffRequest;
import vn.fptu.reasbe.model.dto.user.UpdateResidentRequest;
import vn.fptu.reasbe.model.dto.user.UpdateStaffRequest;
import vn.fptu.reasbe.model.dto.user.UserResponse;
import vn.fptu.reasbe.model.entity.ExchangeRequest;
import vn.fptu.reasbe.model.entity.Feedback;
import vn.fptu.reasbe.model.entity.Item;
import vn.fptu.reasbe.model.entity.User;
import vn.fptu.reasbe.model.enums.core.StatusEntity;
import vn.fptu.reasbe.model.enums.exchange.StatusExchangeHistory;

import java.util.stream.Stream;

/**
 * @author ntig
 */
@Mapper(
        collectionMappingStrategy = CollectionMappingStrategy.SETTER_PREFERRED,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        uses = {
                LocationMapper.class,
                UserLocationMapper.class
        }
)
@Component
public interface UserMapper {
    @Mapping(target = "roleName", source = "role.name")
    @Mapping(target = "numOfExchangedItems", expression = "java(mapNumOfExchangedItems(user))")
    @Mapping(target = "numOfFeedbacks", expression = "java(mapNumOfFeedbacks(user))")
    @Mapping(target = "numOfRatings", expression = "java(mapNumOfRatings(user))")
    UserResponse toUserResponse(User user);

    @Mapping(target = "password", ignore = true)
    User toUser(CreateStaffRequest request);

    @Mapping(target = "firstLogin", constant = "false")
    void updateResident(@MappingTarget User user, UpdateResidentRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateUser(@MappingTarget User user, UpdateStaffRequest request);

    // Count successful exchanges (both as seller and buyer)
    default Integer mapNumOfExchangedItems(User user) {
        if (user.getItems() == null) return 0;

        return (int) user.getItems().stream()
                .flatMap(item -> Stream.concat(
                        item.getSellerExchangeRequests() != null ? item.getSellerExchangeRequests().stream() : Stream.empty(),
                        item.getBuyerExchangeRequests() != null ? item.getBuyerExchangeRequests().stream() : Stream.empty()
                ))
                .map(ExchangeRequest::getExchangeHistory)
                .filter(eh -> eh != null && eh.getStatusExchangeHistory() == StatusExchangeHistory.SUCCESSFUL) // Filter successful exchanges
                .count();
    }

    default boolean isActiveFeedback(Feedback feedback) {
        return feedback != null && feedback.getStatusEntity() == StatusEntity.ACTIVE;
    }

    default Integer mapNumOfFeedbacks(User user) {
        if (user.getItems() == null) return 0;

        return (int) user.getItems().stream()
                .map(Item::getFeedback)
                .filter(this::isActiveFeedback)
                .count();
    }

    default Double mapNumOfRatings(User user) {
        if (user.getItems() == null) return 0.0;

        return user.getItems().stream()
                .map(Item::getFeedback)
                .filter(this::isActiveFeedback)
                .mapToDouble(Feedback::getRating)
                .average()
                .orElse(0.0);
    }
}
