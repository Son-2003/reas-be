package vn.fptu.reasbe.model.dto.criticalreport;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import vn.fptu.reasbe.model.dto.exchange.ExchangeResponse;
import vn.fptu.reasbe.model.dto.feedback.FeedbackResponse;
import vn.fptu.reasbe.model.dto.user.UserResponse;
import vn.fptu.reasbe.model.enums.criticalreport.StatusCriticalReport;
import vn.fptu.reasbe.model.enums.criticalreport.TypeCriticalReport;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CriticalReportResponse {
    Integer id;

    TypeCriticalReport typeReport;

    String contentReport;

    String contentResponse;

    String imageUrl;

    LocalDateTime approvedTime;

    UserResponse user;

    FeedbackResponse feedback;

    ExchangeResponse exchangeRequest;

    UserResponse reporter;

    UserResponse answerer;

    LocalDateTime creationDate;

    LocalDateTime lastModificationDate;

    StatusCriticalReport statusCriticalReport;
}