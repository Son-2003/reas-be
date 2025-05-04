package vn.fptu.reasbe.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.fptu.reasbe.model.dto.core.BaseSearchPaginationResponse;
import vn.fptu.reasbe.model.dto.criticalreport.CriticalReportResidentRequest;
import vn.fptu.reasbe.model.dto.criticalreport.CriticalReportResponse;
import vn.fptu.reasbe.model.dto.criticalreport.CriticalReportStaffRequest;
import vn.fptu.reasbe.model.dto.criticalreport.SearchCriticalReportRequest;
import vn.fptu.reasbe.model.entity.CriticalReport;
import vn.fptu.reasbe.model.entity.ExchangeRequest;
import vn.fptu.reasbe.model.entity.Feedback;
import vn.fptu.reasbe.model.entity.Item;
import vn.fptu.reasbe.model.entity.User;
import vn.fptu.reasbe.model.enums.core.StatusEntity;
import vn.fptu.reasbe.model.enums.criticalreport.StatusCriticalReport;
import vn.fptu.reasbe.model.enums.criticalreport.TypeCriticalReport;
import vn.fptu.reasbe.model.enums.exchange.StatusExchangeHistory;
import vn.fptu.reasbe.model.enums.item.StatusItem;
import vn.fptu.reasbe.model.enums.notification.TypeNotification;
import vn.fptu.reasbe.model.enums.user.RoleName;
import vn.fptu.reasbe.model.exception.ReasApiException;
import vn.fptu.reasbe.model.exception.ResourceNotFoundException;
import vn.fptu.reasbe.model.mongodb.Notification;
import vn.fptu.reasbe.repository.CriticalReportRepository;
import vn.fptu.reasbe.repository.ExchangeRequestRepository;
import vn.fptu.reasbe.repository.FeedbackRepository;
import vn.fptu.reasbe.repository.ItemRepository;
import vn.fptu.reasbe.repository.UserRepository;
import vn.fptu.reasbe.service.AuthService;
import vn.fptu.reasbe.service.CriticalReportService;
import vn.fptu.reasbe.service.VectorStoreService;
import vn.fptu.reasbe.service.mongodb.NotificationService;
import vn.fptu.reasbe.service.mongodb.UserMService;
import vn.fptu.reasbe.utils.common.DateUtils;
import vn.fptu.reasbe.utils.mapper.CriticalReportMapper;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import static vn.fptu.reasbe.model.dto.core.BaseSearchPaginationResponse.getPageable;

@Service
@RequiredArgsConstructor
@Transactional
public class CriticalReportServiceImpl implements CriticalReportService {

    private final CriticalReportRepository criticalReportRepository;
    private final ExchangeRequestRepository exchangeRequestRepository;
    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final AuthService authService;
    private final VectorStoreService vectorStoreService;
    private final UserMService userMService;
    private final NotificationService notificationService;
    private final CriticalReportMapper criticalReportMapper;

    @Override
    public BaseSearchPaginationResponse<CriticalReportResponse> searchCriticalReport(int pageSize, int pageNo, String sortDir, String sortBy, SearchCriticalReportRequest searchRequest) {
        User currUser = authService.getCurrentUser();

        if (currUser.getRole().getName().equals(RoleName.ROLE_RESIDENT)) {
            if (searchRequest == null) {
                searchRequest = new SearchCriticalReportRequest();
            }
            searchRequest.setReporterIds(List.of(currUser.getId()));
        }

        return BaseSearchPaginationResponse.of(criticalReportRepository.searchCriticalReportPagination(searchRequest, getPageable(pageNo, pageSize, sortDir, sortBy))
                .map(criticalReportMapper::toCriticalReportResponse));
    }

    @Override
    public CriticalReportResponse getCriticalReport(Integer id) {
        User currentUser = authService.getCurrentUser();

        CriticalReport criticalReport = getCriticalReportById(id);

        if (currentUser.getRole().getName().equals(RoleName.ROLE_RESIDENT) && !Objects.equals(currentUser, criticalReport.getReporter())) {
            throw new ReasApiException(HttpStatus.FORBIDDEN, "error.userNotAllowed");
        }

        return criticalReportMapper.toCriticalReportResponse(criticalReport);
    }

    @Override
    public CriticalReportResponse createCriticalReport(CriticalReportResidentRequest request) {
        User currentUser = authService.getCurrentUser();
        CriticalReport criticalReport = criticalReportMapper.toCriticalReport(request);

        ExchangeRequest exchange = processTargetEntity(request, criticalReport, currentUser);

        criticalReport.setReporter(currentUser);

        if (exchange != null) {
            processAutoApprovalIfExists(exchange, criticalReport, currentUser);
        } else {
            criticalReport.setStatusCriticalReport(StatusCriticalReport.PENDING);
        }

        return criticalReportMapper.toCriticalReportResponse(criticalReportRepository.save(criticalReport));
    }

    private ExchangeRequest processTargetEntity(CriticalReportResidentRequest request, CriticalReport criticalReport, User currentUser) {
        if (request.getExchangeId() != null) {
            return handleExchangeReport(request.getExchangeId(), criticalReport, currentUser);
        } else if (request.getFeedbackId() != null) {
            handleFeedbackReport(request.getFeedbackId(), criticalReport, currentUser);
        } else if (request.getResidentId() != null) {
            handleUserReport(request.getResidentId(), criticalReport, currentUser);
        } else {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.mustHaveUserOrFeedbackOrExchange");
        }
        return null;
    }

    private ExchangeRequest handleExchangeReport(Integer exchangeId, CriticalReport criticalReport, User currentUser) {
        ExchangeRequest exchange = exchangeRequestRepository.findById(exchangeId)
                .orElseThrow(() -> new ResourceNotFoundException("Exchange request", "id", exchangeId));

        if (exchange.getExchangeHistory() != null) {
            StatusExchangeHistory status = exchange.getExchangeHistory().getStatusExchangeHistory();
            if (!(status == StatusExchangeHistory.NOT_YET_EXCHANGE || status == StatusExchangeHistory.PENDING_EVIDENCE)) {
                throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.cannotReportExchange");
            }
        }

        User seller = exchange.getSellerItem().getOwner();
        User buyer = exchange.getBuyerItem() != null ? exchange.getBuyerItem().getOwner() : exchange.getPaidBy();
        if (!Objects.equals(seller, currentUser) && !Objects.equals(buyer, currentUser)) {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.invalidReportedExchange");
        }

        criticalReport.setExchangeRequest(exchange);
        criticalReport.setTypeReport(TypeCriticalReport.EXCHANGE);
        return exchange;
    }

    private void handleFeedbackReport(Integer feedbackId, CriticalReport criticalReport, User currentUser) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback", "id", feedbackId));

        if (Objects.equals(feedback.getUser(), currentUser)) {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.cannotReportYourOwnFeedback");
        }

        criticalReport.setFeedback(feedback);
        criticalReport.setTypeReport(TypeCriticalReport.FEEDBACK);
    }

    private void handleUserReport(Integer residentId, CriticalReport criticalReport, User currentUser) {
        if (Objects.equals(residentId, currentUser.getId())) {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.cannotReportYourself");
        }

        User user = userRepository.findById(residentId)
                .orElseThrow(() -> new ResourceNotFoundException("Resident", "id", residentId));

        criticalReport.setResident(user);
        criticalReport.setTypeReport(TypeCriticalReport.RESIDENT);
    }

    private void processAutoApprovalIfExists(ExchangeRequest exchange, CriticalReport criticalReport, User currentUser) {
        CriticalReport existed = criticalReportRepository.findByExchange(exchange);
        if (existed != null) {
            handleAutoApprovalCriticalReportUpdate(existed);
            handleAutoApprovalCriticalReportUpdate(criticalReport);
            criticalReportRepository.save(existed);

            vn.fptu.reasbe.model.mongodb.User sender = userMService.getAdmin();
            vn.fptu.reasbe.model.mongodb.User recipient = userMService.findByUsername(currentUser.getUserName());
            vn.fptu.reasbe.model.mongodb.User otherRecipient = userMService.findByUsername(
                    getOtherResidentOfExchange(currentUser, exchange).getUserName());

            setExchangeRequestFailedAndSendNotification(exchange, sender, recipient, otherRecipient);
        } else {
            criticalReport.setStatusCriticalReport(StatusCriticalReport.PENDING);
        }
    }


    void handleAutoApprovalCriticalReportUpdate(CriticalReport report) {
        report.setAnswerer(userRepository.findByUserName("admin")
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", "admin")));
        report.setResolvedTime(DateUtils.getCurrentDateTime());
        report.setContentResponse("Auto approval due to both reports from both resident");
        report.setStatusCriticalReport(StatusCriticalReport.RESOLVED);
    }

    @Override
    public CriticalReportResponse reviewCriticalReport(CriticalReportStaffRequest request) {
        User currentUser = authService.getCurrentUser();

        CriticalReport existedReport = getCriticalReportById(request.getId());

        if (existedReport.getExchangeRequest() != null &&
                !(existedReport.getExchangeRequest().getExchangeHistory().getStatusExchangeHistory().equals(StatusExchangeHistory.NOT_YET_EXCHANGE) ||
                        existedReport.getExchangeRequest().getExchangeHistory().getStatusExchangeHistory().equals(StatusExchangeHistory.PENDING_EVIDENCE))) {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.exchangeNotAllowedForReport");
        }

        criticalReportMapper.updateCriticalReport(existedReport, request);
        existedReport.setAnswerer(currentUser);

        if (Boolean.TRUE.equals(request.getIsResolved())) {
            existedReport.setStatusCriticalReport(StatusCriticalReport.RESOLVED);
            existedReport.setResolvedTime(DateUtils.getCurrentDateTime());
        } else {
            existedReport.setStatusCriticalReport(StatusCriticalReport.REJECTED);
        }

        vn.fptu.reasbe.model.mongodb.User sender = userMService.findByUsername(currentUser.getUserName());
        vn.fptu.reasbe.model.mongodb.User recipient = userMService.findByUsername(existedReport.getReporter().getUserName());
        Notification notification = new Notification(sender.getUserName(), recipient.getUserName(),
                "Report #" + existedReport.getId() + " has been marked as " +
                        existedReport.getStatusCriticalReport().toString().toLowerCase() +
                        " by "+ sender.getFullName() + ".",
                new Date(), TypeNotification.REPORT_RESPONSE, recipient.getRegistrationTokens());

        notificationService.saveAndSendNotification(notification);

        if (existedReport.getStatusCriticalReport().equals(StatusCriticalReport.RESOLVED)) {
            if (existedReport.getTypeReport().equals(TypeCriticalReport.EXCHANGE)) {
                vn.fptu.reasbe.model.mongodb.User otherRecipient = userMService.findByUsername(getOtherResidentOfExchange(existedReport.getReporter(), existedReport.getExchangeRequest()).getUserName());
                setExchangeRequestFailedAndSendNotification(existedReport.getExchangeRequest(), sender, recipient, otherRecipient);
            } else if (existedReport.getTypeReport().equals(TypeCriticalReport.FEEDBACK)) {
                vn.fptu.reasbe.model.mongodb.User otherRecipient = userMService.findByUsername(existedReport.getFeedback().getUser().getUserName());
                setFeedbackInactiveAndSendNotification(existedReport.getFeedback(), sender, otherRecipient);
            }
        }

        return criticalReportMapper.toCriticalReportResponse(criticalReportRepository.save(existedReport));
    }

    private void setFeedbackInactiveAndSendNotification(Feedback feedback, vn.fptu.reasbe.model.mongodb.User sender, vn.fptu.reasbe.model.mongodb.User recipient) {
        feedback.setStatusEntity(StatusEntity.INACTIVE);
        feedbackRepository.save(feedback);

        //Feedback deleted notification to recipient
        Notification notification1 = new Notification(sender.getUserName(), recipient.getUserName(),
                "Feedback for item " + feedback.getItem().getItemName() + " has been removed due to a report toward the feedback",
                new Date(), TypeNotification.REPORT_HANDLED, recipient.getRegistrationTokens());

        notificationService.saveAndSendNotification(notification1);
    }

    private CriticalReport getCriticalReportById(Integer id) {
        return criticalReportRepository.findCriticalReportById(id).orElseThrow(() -> new ResourceNotFoundException("CriticalReport", "id", id));
    }

    private void setExchangeRequestFailedAndSendNotification(ExchangeRequest exchangeRequest, vn.fptu.reasbe.model.mongodb.User sender, vn.fptu.reasbe.model.mongodb.User recipient1, vn.fptu.reasbe.model.mongodb.User recipient2) {
        exchangeRequest.getExchangeHistory().setStatusExchangeHistory(StatusExchangeHistory.FAILED);
        //Exchange FAILED notification to recipient 1
        Notification notification1 = new Notification(sender.getUserName(), recipient1.getUserName(),
                "Exchange #EX" + exchangeRequest.getId() + " has been failed due to a report toward the exchange",
                new Date(), TypeNotification.REPORT_HANDLED, recipient1.getRegistrationTokens());

        //Exchange FAILED notification to recipient 2
        Notification notification2 = new Notification(sender.getUserName(), recipient2.getUserName(),
                "Exchange #EX" + exchangeRequest.getId() + " has been failed due to a report toward the exchange",
                new Date(), TypeNotification.REPORT_HANDLED, recipient2.getRegistrationTokens());

        notificationService.saveAndSendNotification(notification1);
        notificationService.saveAndSendNotification(notification2);

        checkExpiredItemAfterFailedExchange(exchangeRequest.getSellerItem());
        if (exchangeRequest.getBuyerItem() != null) {
            checkExpiredItemAfterFailedExchange(exchangeRequest.getBuyerItem());
        }

        exchangeRequestRepository.save(exchangeRequest);
    }

    private User getOtherResidentOfExchange(User reporter, ExchangeRequest exchangeRequest) {
        if (Objects.equals(exchangeRequest.getSellerItem().getOwner(), reporter)) {
            return exchangeRequest.getBuyerItem() != null ? exchangeRequest.getBuyerItem().getOwner() : exchangeRequest.getPaidBy();
        } else {
            return exchangeRequest.getSellerItem().getOwner();
        }
    }

    private void checkExpiredItemAfterFailedExchange(Item item) {
        if (item.getExpiredTime().isBefore(DateUtils.getCurrentDateTime())) {
            item.setStatusItem(StatusItem.EXPIRED);

            vn.fptu.reasbe.model.mongodb.User recipient = userMService.findByUsername(item.getOwner().getUserName());
            Notification notification = new Notification(userMService.getAdmin().getUserName(), recipient.getUserName(),
                    "Your item " + item.getItemName() + " has expired",
                    new Date(), TypeNotification.ITEM_EXPIRED, recipient.getRegistrationTokens());
            notificationService.saveAndSendNotification(notification);

        } else {
            item.setStatusItem(StatusItem.AVAILABLE);
            vectorStoreService.addNewItem(List.of(item));
        }

        itemRepository.save(item);
    }
}
