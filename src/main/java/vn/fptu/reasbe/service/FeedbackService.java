package vn.fptu.reasbe.service;

import vn.fptu.reasbe.model.dto.core.BaseSearchPaginationResponse;
import vn.fptu.reasbe.model.dto.feedback.FeedbackRequest;
import vn.fptu.reasbe.model.dto.feedback.FeedbackResponse;
import vn.fptu.reasbe.model.enums.core.StatusEntity;

import java.util.List;

public interface FeedbackService {
    BaseSearchPaginationResponse<FeedbackResponse> getAllFeedbackOfUser(int pageNo, int pageSize, String sortBy, String sortDir, Integer userId, Integer rating, List<StatusEntity> statusEntities);

    FeedbackResponse viewFeedbackDetail(Integer feedbackId);

    FeedbackResponse createFeedback(FeedbackRequest feedbackRequest);

    FeedbackResponse updateFeedback(FeedbackRequest feedbackRequest);

    Boolean deleteFeedback(Integer feedbackId);
}
