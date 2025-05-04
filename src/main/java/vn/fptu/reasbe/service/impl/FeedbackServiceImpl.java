package vn.fptu.reasbe.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import vn.fptu.reasbe.model.dto.core.BaseSearchPaginationResponse;
import vn.fptu.reasbe.model.dto.feedback.FeedbackRequest;
import vn.fptu.reasbe.model.dto.feedback.FeedbackResponse;
import vn.fptu.reasbe.model.entity.ExchangeHistory;
import vn.fptu.reasbe.model.entity.Feedback;
import vn.fptu.reasbe.model.entity.Item;
import vn.fptu.reasbe.model.entity.User;
import vn.fptu.reasbe.model.enums.core.StatusEntity;
import vn.fptu.reasbe.model.enums.exchange.StatusExchangeHistory;
import vn.fptu.reasbe.model.enums.user.RoleName;
import vn.fptu.reasbe.model.exception.ReasApiException;
import vn.fptu.reasbe.model.exception.ResourceNotFoundException;
import vn.fptu.reasbe.repository.ExchangeHistoryRepository;
import vn.fptu.reasbe.repository.FeedbackRepository;
import vn.fptu.reasbe.service.AuthService;
import vn.fptu.reasbe.service.FeedbackService;
import vn.fptu.reasbe.service.ItemService;
import vn.fptu.reasbe.service.UserService;
import vn.fptu.reasbe.utils.mapper.FeedbackMapper;

import java.util.List;

import static vn.fptu.reasbe.model.dto.core.BaseSearchPaginationResponse.getPageable;

@Service
@Transactional
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final ExchangeHistoryRepository exchangeHistoryRepository;
    private final AuthService authService;
    private final UserService userService;
    private final ItemService itemService;
    private final FeedbackMapper feedbackMapper;

    @Override
    public BaseSearchPaginationResponse<FeedbackResponse> getAllFeedbackOfUser(int pageNo, int pageSize, String sortBy, String sortDir, Integer userId, Integer rating, List<StatusEntity> statusEntities) {

        User currentUser = authService.getCurrentUser();
        User user = userService.getUserById(userId);

        boolean isResident = currentUser == null || RoleName.ROLE_RESIDENT.equals(currentUser.getRole().getName());

        List<StatusEntity> statusList;

        if (isResident) {
            statusList = List.of(StatusEntity.ACTIVE);
        } else if (statusEntities == null || statusEntities.isEmpty()) {
            statusList = List.of(StatusEntity.ACTIVE, StatusEntity.INACTIVE);
        } else {
            statusList = statusEntities;
        }

        if (rating != null && (rating < 1 || rating > 5)) {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.invalidRating");
        }

        Page<Feedback> feedbacks;

        if (rating != null) {
            feedbacks = feedbackRepository.getAllByItemOwnerAndRatingAndStatusEntityIn(user, rating, statusList, getPageable(pageNo, pageSize, sortBy, sortDir));
        } else {
            feedbacks = feedbackRepository.getAllByItemOwnerAndStatusEntityIn(user, statusList, getPageable(pageNo, pageSize, sortBy, sortDir));
        }

        return BaseSearchPaginationResponse.of(feedbacks.map(feedbackMapper::toFeedbackResponse));
    }


    @Override
    public FeedbackResponse viewFeedbackDetail(Integer feedbackId) {
        return feedbackMapper.toFeedbackResponse(getFeedbackById(feedbackId));
    }

    @Override
    public FeedbackResponse createFeedback(FeedbackRequest feedbackRequest) {
        User user = authService.getCurrentUser();

        ExchangeHistory exchangeHistory = exchangeHistoryRepository.findById(feedbackRequest.getExchangeHistoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Exchange History", "id", feedbackRequest.getExchangeHistoryId()));

        if (!exchangeHistory.getStatusExchangeHistory().equals(StatusExchangeHistory.SUCCESSFUL)) {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.exchangeHistoryNotSuccessful");
        }

        Item item = itemService.getItemById(feedbackRequest.getItemId());

        if (item.getFeedback() != null) {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.feedbackAlreadyExists");
        }

        Feedback feedback = feedbackMapper.toFeedback(feedbackRequest);
        feedback.setUser(user);
        feedback.setItem(item);
        feedback.setExchangeHistory(exchangeHistory);
        feedback.setUpdated(Boolean.FALSE);
        return feedbackMapper.toFeedbackResponse(feedbackRepository.save(feedback));
    }

    @Override
    public FeedbackResponse updateFeedback(FeedbackRequest feedbackRequest) {
        User user = authService.getCurrentUser();

        Feedback feedback = getFeedbackById(feedbackRequest.getId());

        if (feedback.isUpdated()) {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.onlyUpdateFeedbackOnce");
        }

        if (!feedback.getUser().equals(user)) {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.userNotAllowed");
        }

        feedback.setUpdated(Boolean.TRUE);
        feedbackMapper.updateFeedback(feedback, feedbackRequest);
        return feedbackMapper.toFeedbackResponse(feedbackRepository.save(feedback));
    }

    @Override
    public Boolean deleteFeedback(Integer feedbackId) {
        Feedback feedback = getFeedbackById(feedbackId);
        feedback.setStatusEntity(StatusEntity.INACTIVE);
        feedbackRepository.save(feedback);
        return true;
    }

    private Feedback getFeedbackById(Integer feedbackId) {
        return feedbackRepository.findByIdAndStatusEntity(feedbackId, StatusEntity.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback", "id", feedbackId));
    }
}
