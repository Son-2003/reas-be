package vn.fptu.reasbe.model.dto.feedback;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import vn.fptu.reasbe.model.dto.exchange.ExchangeHistoryResponse;
import vn.fptu.reasbe.model.dto.item.ItemResponse;
import vn.fptu.reasbe.model.dto.user.UserResponse;
import vn.fptu.reasbe.model.enums.core.StatusEntity;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FeedbackResponse {

    Integer id;

    UserResponse user;

    ExchangeHistoryResponse exchangeHistory;

    ItemResponse item;

    Integer rating;

    String comment;

    String imageUrl;

    LocalDateTime creationDate;

    boolean isUpdated;

    StatusEntity statusEntity;
}