package vn.fptu.reasbe.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.fptu.reasbe.model.constant.AppConstants;
import vn.fptu.reasbe.model.dto.core.BaseSearchPaginationResponse;
import vn.fptu.reasbe.model.dto.feedback.FeedbackRequest;
import vn.fptu.reasbe.model.dto.feedback.FeedbackResponse;
import vn.fptu.reasbe.model.enums.core.StatusEntity;
import vn.fptu.reasbe.service.FeedbackService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @GetMapping()
    public ResponseEntity<BaseSearchPaginationResponse<FeedbackResponse>> getAllFeedbackOfUser(
            @RequestParam(name = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(name = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDir,
            @RequestParam Integer userId,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) List<StatusEntity> statusEntities
    ) {
        return ResponseEntity.ok(feedbackService.getAllFeedbackOfUser(pageNo, pageSize, sortBy, sortDir, userId, rating, statusEntities));
    }

    @GetMapping("/{feedbackId}")
    public ResponseEntity<FeedbackResponse> viewFeedbackDetail(@PathVariable Integer feedbackId) {
        return ResponseEntity.ok(feedbackService.viewFeedbackDetail(feedbackId));
    }

    @PostMapping()
    @PreAuthorize("hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_RESIDENT)")
    public ResponseEntity<FeedbackResponse> createFeedback(@RequestBody @Valid FeedbackRequest feedbackRequest) {
        return ResponseEntity.ok(feedbackService.createFeedback(feedbackRequest));
    }

    @PutMapping()
    @PreAuthorize("hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_RESIDENT)")
    public ResponseEntity<FeedbackResponse> updateFeedback(@RequestBody @Valid FeedbackRequest feedbackRequest) {
        return ResponseEntity.ok(feedbackService.updateFeedback(feedbackRequest));
    }

    @DeleteMapping()
    @PreAuthorize("hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_STAFF)")
    public ResponseEntity<Boolean> deleteFeedback(@RequestParam Integer feedbackId) {
        return ResponseEntity.ok(feedbackService.deleteFeedback(feedbackId));
    }
}
