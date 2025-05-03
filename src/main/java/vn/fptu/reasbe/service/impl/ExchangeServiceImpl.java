package vn.fptu.reasbe.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.fptu.reasbe.model.constant.AppConstants;
import vn.fptu.reasbe.model.dto.core.BaseSearchPaginationResponse;
import vn.fptu.reasbe.model.dto.exchange.EvidenceExchangeRequest;
import vn.fptu.reasbe.model.dto.exchange.ExchangeRequestRequest;
import vn.fptu.reasbe.model.dto.exchange.ExchangeResponse;
import vn.fptu.reasbe.model.entity.ExchangeHistory;
import vn.fptu.reasbe.model.entity.ExchangeRequest;
import vn.fptu.reasbe.model.entity.Item;
import vn.fptu.reasbe.model.entity.User;
import vn.fptu.reasbe.model.enums.core.StatusEntity;
import vn.fptu.reasbe.model.enums.exchange.StatusExchangeHistory;
import vn.fptu.reasbe.model.enums.exchange.StatusExchangeRequest;
import vn.fptu.reasbe.model.enums.item.StatusItem;
import vn.fptu.reasbe.model.enums.notification.TypeNotification;
import vn.fptu.reasbe.model.enums.user.RoleName;
import vn.fptu.reasbe.model.exception.ReasApiException;
import vn.fptu.reasbe.model.exception.ResourceNotFoundException;
import vn.fptu.reasbe.model.mongodb.Notification;
import vn.fptu.reasbe.repository.ExchangeHistoryRepository;
import vn.fptu.reasbe.repository.ExchangeRequestRepository;
import vn.fptu.reasbe.service.AuthService;
import vn.fptu.reasbe.service.ExchangeService;
import vn.fptu.reasbe.service.ItemService;
import vn.fptu.reasbe.service.UserService;
import vn.fptu.reasbe.service.VectorStoreService;
import vn.fptu.reasbe.service.mongodb.NotificationService;
import vn.fptu.reasbe.service.mongodb.UserMService;
import vn.fptu.reasbe.utils.common.DateUtils;
import vn.fptu.reasbe.utils.mapper.ExchangeRequestMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static vn.fptu.reasbe.model.dto.core.BaseSearchPaginationResponse.getPageable;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ExchangeServiceImpl implements ExchangeService {

    private final ItemService itemService;
    private final UserService userService;
    private final AuthService authService;
    private final VectorStoreService vectorStoreService;
    private final NotificationService notificationService;
    private final UserMService userMService;
    private final ExchangeRequestRepository exchangeRequestRepository;
    private final ExchangeHistoryRepository exchangeHistoryRepository;
    private final ExchangeRequestMapper exchangeMapper;

    @Override
    public BaseSearchPaginationResponse<ExchangeResponse> getAllExchangeByStatusOfCurrentUser(int pageNo, int pageSize, String sortBy, String sortDir,
                                                                                              StatusExchangeRequest statusRequest, StatusExchangeHistory statusHistory) {
        User user = getCurrentUser();
        Pageable pageable = getPageable(pageNo, pageSize, sortBy, sortDir);
        if (statusRequest.equals(StatusExchangeRequest.APPROVED)) {
            if (statusHistory == null) {
                return BaseSearchPaginationResponse.of(exchangeRequestRepository.findByExchangeHistoryStatusInAndUser(
                                List.of(StatusExchangeHistory.NOT_YET_EXCHANGE, StatusExchangeHistory.PENDING_EVIDENCE), user, pageable)
                        .map(exchangeMapper::toExchangeResponse));
            } else {
                return BaseSearchPaginationResponse.of(exchangeRequestRepository.findByExchangeHistoryStatusAndUser(statusHistory, user, pageable)
                        .map(exchangeMapper::toExchangeResponse));
            }
        } else {
            return BaseSearchPaginationResponse.of(exchangeRequestRepository.findByExchangeRequestStatusAndUser(statusRequest, user, pageable)
                    .map(exchangeMapper::toExchangeResponse));
        }
    }

    @Override
    public BaseSearchPaginationResponse<ExchangeResponse> getAllExchangeHistoryOfUser(int pageNo, int pageSize, String sortBy, String sortDir, Integer userId) {
        User resident = userService.getUserById(userId);

        if (!resident.getRole().getName().equals(RoleName.ROLE_RESIDENT)) {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.userNotResident");
        }

        Pageable pageable = getPageable(pageNo, pageSize, sortBy, sortDir);

        return BaseSearchPaginationResponse.of(exchangeRequestRepository.findByExchangeRequestStatusAndUser(StatusExchangeRequest.APPROVED, resident, pageable)
                .map(exchangeMapper::toExchangeResponse));
    }

    @Override
    public ExchangeResponse getExchangeById(Integer id) {
        User user = getCurrentUser();
        ExchangeRequest request = getExchangeRequestById(id);

        if (user.getRole().getName().equals(RoleName.ROLE_RESIDENT)) {
            boolean isSeller = Objects.equals(request.getSellerItem().getOwner(), user);
            boolean isBuyer = Objects.equals(getBuyer(request), user);

            if (!isSeller && !isBuyer) {
                throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.userNotAllowed");
            }
        }

        return exchangeMapper.toExchangeResponse(request);
    }

    @Override
    public ExchangeResponse createExchangeRequest(ExchangeRequestRequest exchangeRequestRequest) {
        User currentUser = getCurrentUser();

        validateExchangeRequest(exchangeRequestRequest);

        Item sellerItem = itemService.getItemById(exchangeRequestRequest.getSellerItemId());
        if (Objects.equals(sellerItem.getOwner(), currentUser)) {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.invalidSellerItem");
        }

        if (!sellerItem.getStatusItem().equals(StatusItem.AVAILABLE)) {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.sellerItemNotAvailable");
        }

        if (sellerItem.getMethodExchanges().stream()
                .noneMatch(method -> method.equals(exchangeRequestRequest.getMethodExchange()))) {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.methodExchangeNotMatch");
        }

        ExchangeRequest request = exchangeMapper.toExchangeRequest(exchangeRequestRequest);

        if (exchangeRequestRequest.getBuyerItemId() != null) {
            Item buyerItem = itemService.getItemById(exchangeRequestRequest.getBuyerItemId());
            if (!Objects.equals(buyerItem.getOwner(), currentUser)) {
                throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.invalidBuyerItem");
            }
            if (!buyerItem.getStatusItem().equals(StatusItem.AVAILABLE)) {
                throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.buyerItemNotAvailable");
            }
            if (sellerItem.getOwner().equals(buyerItem.getOwner())) {
                throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.sameOwnerWithSellerAndBuyerItem");
            }

            request.setBuyerItem(buyerItem);
        } else {
            if (!sellerItem.isMoneyAccepted()) {
                throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.buyerItemNull");
            }
        }

        User paidByUser = userService.getUserById(exchangeRequestRequest.getPaidByUserId());

        request.setSellerItem(sellerItem);
        request.setPaidBy(paidByUser);
        request.setNumberOfOffer(AppConstants.NUM_OF_OFFER);
        request.setStatusExchangeRequest(StatusExchangeRequest.PENDING);

        if (sellerItem.getPrice().equals(BigDecimal.ZERO)) {
            request.setSellerConfirmation(Boolean.TRUE);
            request.setBuyerConfirmation(Boolean.TRUE);
        } else {
            request.setBuyerConfirmation(Boolean.FALSE);
            request.setSellerConfirmation(Boolean.FALSE);
        }

        ExchangeRequest savedRequest = exchangeRequestRepository.save(request);

        // Send notification to seller
        vn.fptu.reasbe.model.mongodb.User sender = userMService.findByUsername(currentUser.getUserName());
        vn.fptu.reasbe.model.mongodb.User recipient = userMService.findByUsername(sellerItem.getOwner().getUserName());
        Notification notification = new Notification(sender.getUserName(), recipient.getUserName(),
                "New exchange request #EX" + savedRequest.getId() + " with your " + sellerItem.getItemName(),
                new Date(), TypeNotification.EXCHANGE_REQUEST, recipient.getRegistrationTokens());
        notificationService.saveAndSendNotification(notification);

        return exchangeMapper.toExchangeResponse(savedRequest);
    }

    private User getCurrentUser() {
        return authService.getCurrentUser();
    }

    @Override
    public ExchangeResponse updateExchangeRequestPrice(Integer id, BigDecimal negotiatedPrice) {
        User user = getCurrentUser();
        String recipientName;

        ExchangeRequest request = getExchangeRequestById(id);

        checkIfExchangeIsPending(request);

        if (negotiatedPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.negativePrice");
        }

        if (request.getNumberOfOffer().equals(0)) {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.noOfferLeft");
        }

        request.setFinalPrice(negotiatedPrice);
        request.setNumberOfOffer(request.getNumberOfOffer() - 1);

        //Checking and changing status for confirmation from both user
        if (Objects.equals(getBuyer(request), user)) {
            if (request.getBuyerConfirmation().equals(Boolean.TRUE)) {
                throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.waitForOtherUserConfirmation");
            }
            request.setSellerConfirmation(Boolean.FALSE);
            request.setBuyerConfirmation(Boolean.TRUE);
            recipientName = request.getSellerItem().getOwner().getUserName();
        } else if (Objects.equals(request.getSellerItem().getOwner(), user)) {
            if (request.getSellerConfirmation().equals(Boolean.TRUE)) {
                throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.waitForOtherUserConfirmation");
            }
            request.setSellerConfirmation(Boolean.TRUE);
            request.setBuyerConfirmation(Boolean.FALSE);
            recipientName = getBuyer(request).getUserName();
        } else {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.userNotAllowed");
        }

        // Send notification
        vn.fptu.reasbe.model.mongodb.User sender = userMService.findByUsername(user.getUserName());
        vn.fptu.reasbe.model.mongodb.User recipient = userMService.findByUsername(recipientName);
        Notification notification = new Notification(sender.getUserName(), recipient.getUserName(),
                "New negotiated price offered for exchange #EX" + request.getId(),
                new Date(), TypeNotification.EXCHANGE_REQUEST, recipient.getRegistrationTokens());
        notificationService.saveAndSendNotification(notification);

        return exchangeMapper.toExchangeResponse(exchangeRequestRepository.save(request));
    }

    @Override
    public ExchangeResponse reviewExchangeRequest(Integer id, StatusExchangeRequest statusExchangeRequest) {
        ExchangeRequest request = getExchangeRequestById(id);

        User currentUser = getCurrentUser();

        if (!Objects.equals(request.getSellerItem().getOwner(), currentUser)) {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.userNotAllowed");
        }

        checkIfExchangeIsPending(request);

        if (statusExchangeRequest.equals(StatusExchangeRequest.PENDING) || statusExchangeRequest.equals(StatusExchangeRequest.CANCELLED)) {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.statusExchangeRequestInvalid");
        }

        if (statusExchangeRequest.equals(StatusExchangeRequest.APPROVED)) {
            if (request.getSellerConfirmation().equals(Boolean.FALSE) || request.getBuyerConfirmation().equals(Boolean.FALSE)) {
                throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.notYetConfirmFinalPrice");
            }
            request.getSellerItem().setStatusItem(StatusItem.IN_EXCHANGE);

            List<Item> deletedItemsFromVectorStore = new ArrayList<>();
            deletedItemsFromVectorStore.add(request.getSellerItem());

            if (request.getBuyerItem() != null) {
                request.getBuyerItem().setStatusItem(StatusItem.IN_EXCHANGE);
                deletedItemsFromVectorStore.add(request.getBuyerItem());
            }

            ExchangeHistory exchangeHistory = new ExchangeHistory();
            exchangeHistory.setSellerConfirmation(Boolean.FALSE);
            exchangeHistory.setBuyerConfirmation(Boolean.FALSE);
            exchangeHistory.setStatusExchangeHistory(StatusExchangeHistory.NOT_YET_EXCHANGE);

            cancelOtherExchangeRequests(request.getSellerItem(), request.getBuyerItem(), request.getId());

            vectorStoreService.deleteItem(deletedItemsFromVectorStore);

            request.setExchangeHistory(exchangeHistoryRepository.save(exchangeHistory));
        }

        request.setStatusExchangeRequest(statusExchangeRequest);

        // Send notification
        vn.fptu.reasbe.model.mongodb.User sender = userMService.findByUsername(currentUser.getUserName());
        vn.fptu.reasbe.model.mongodb.User recipient = userMService.findByUsername(getBuyer(request).getUserName());
        Notification notification = new Notification(sender.getUserName(), recipient.getUserName(),
                "Your exchange request #EX" + request.getId() + " has been " + String.valueOf(statusExchangeRequest).toLowerCase(),
                new Date(), TypeNotification.EXCHANGE_REQUEST, recipient.getRegistrationTokens());
        notificationService.saveAndSendNotification(notification);

        return exchangeMapper.toExchangeResponse(exchangeRequestRepository.save(request));
    }

    @Override
    public ExchangeResponse cancelExchange(Integer id) {
        ExchangeRequest request = getExchangeRequestById(id);

        User currentUser = getCurrentUser();

        if (!Objects.equals(getBuyer(request), currentUser)) {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.userNotAllowed");
        }

        ExchangeResponse response;

        if (request.getStatusExchangeRequest().equals(StatusExchangeRequest.PENDING)) {
            response = exchangeMapper.toExchangeResponse(cancelExchangeRequest(request));
        } else if (request.getStatusExchangeRequest().equals(StatusExchangeRequest.APPROVED)) {
            response = exchangeMapper.toExchangeResponse(cancelApprovedExchange(request));
        } else {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.cannotCancelExchange");
        }

        vn.fptu.reasbe.model.mongodb.User sender = userMService.findByUsername(currentUser.getUserName());
        vn.fptu.reasbe.model.mongodb.User recipient = userMService.findByUsername(request.getSellerItem().getOwner().getUserName());
        Notification notification = new Notification(sender.getUserName(), recipient.getUserName(),
                "Your exchange request #EX" + request.getId() + " has been cancelled",
                new Date(), TypeNotification.EXCHANGE_REQUEST, recipient.getRegistrationTokens());
        notificationService.saveAndSendNotification(notification);

        return response;
    }

    @Override
    public ExchangeResponse confirmNegotiatedPrice(Integer id) {
        User user = getCurrentUser();

        ExchangeRequest request = getExchangeRequestById(id);

        checkIfExchangeIsPending(request);

        vn.fptu.reasbe.model.mongodb.User sender;
        vn.fptu.reasbe.model.mongodb.User recipient;

        if (Objects.equals(request.getSellerItem().getOwner(), user)) { //checking if the current user is the seller
            request.setSellerConfirmation(Boolean.TRUE);
            sender = userMService.findByUsername(request.getSellerItem().getOwner().getUserName());
            recipient = userMService.findByUsername(getBuyer(request).getUserName());
        } else if (Objects.equals(getBuyer(request), user)) {
            request.setBuyerConfirmation(Boolean.TRUE);
            sender = userMService.findByUsername(getBuyer(request).getUserName());
            recipient = userMService.findByUsername(request.getSellerItem().getOwner().getUserName());
        } else {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.userNotAllowed");
        }

        Notification notification = new Notification(sender.getUserName(), recipient.getUserName(),
                sender.getFullName() + " confirm the negotiated price offered for exchange #EX" + request.getId(),
                new Date(), TypeNotification.EXCHANGE_REQUEST, recipient.getRegistrationTokens());
        notificationService.saveAndSendNotification(notification);

        return exchangeMapper.toExchangeResponse(exchangeRequestRepository.save(request));
    }

    @Override
    public ExchangeResponse uploadEvidence(EvidenceExchangeRequest request) {
        User user = getCurrentUser();

        ExchangeHistory exchangeHistory = exchangeHistoryRepository.findById(request.getExchangeHistoryId())
                .orElseThrow(() -> new ResourceNotFoundException("ExchangeHistory", "id", request.getExchangeHistoryId()));

        if (DateUtils.getCurrentDateTime().isBefore(exchangeHistory.getExchangeRequest().getExchangeDate())) {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.notPassExchangeDateYet");
        }

        if (user.equals(exchangeHistory.getExchangeRequest().getSellerItem().getOwner())) { //checking if the current user is the seller -> upload on seller side
            exchangeHistory.setSellerConfirmation(Boolean.TRUE);
            exchangeHistory.setSellerImageUrl(request.getImageUrl());
            exchangeHistory.setSellerAdditionalNotes(request.getAdditionalNotes());
        } else if (user.equals(getBuyer(exchangeHistory.getExchangeRequest()))) {
            exchangeHistory.setBuyerConfirmation(Boolean.TRUE);
            exchangeHistory.setBuyerImageUrl(request.getImageUrl());
            exchangeHistory.setBuyerAdditionalNotes(request.getAdditionalNotes());
        } else {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.userNotAllowed");
        }

        // Send notification
        vn.fptu.reasbe.model.mongodb.User sender = userMService.findByUsername(user.getUserName());
        vn.fptu.reasbe.model.mongodb.User recipient = userMService.findByUsername(user.equals(exchangeHistory.getExchangeRequest().getSellerItem().getOwner())
                ? getBuyer(exchangeHistory.getExchangeRequest()).getUserName() : exchangeHistory.getExchangeRequest().getSellerItem().getOwner().getUserName());
        Notification notification = new Notification(sender.getUserName(), recipient.getUserName(),
                "Evidence of exchange #EX" + exchangeHistory.getExchangeRequest().getId() + " has been uploaded",
                new Date(), TypeNotification.EXCHANGE_REQUEST, recipient.getRegistrationTokens());
        notificationService.saveAndSendNotification(notification);

        if (Boolean.TRUE.equals(exchangeHistory.getBuyerConfirmation()) &&
                Boolean.TRUE.equals(exchangeHistory.getSellerConfirmation())) {
            exchangeHistory.setStatusExchangeHistory(StatusExchangeHistory.SUCCESSFUL);
            exchangeHistory.getExchangeRequest().getSellerItem().setStatusItem(StatusItem.EXCHANGED);
            if (exchangeHistory.getExchangeRequest().getBuyerItem() != null) {
                exchangeHistory.getExchangeRequest().getBuyerItem().setStatusItem(StatusItem.EXCHANGED);
            }

            vn.fptu.reasbe.model.mongodb.User senderAdmin = userMService.getAdmin();

            vn.fptu.reasbe.model.mongodb.User recipient1 = userMService.findByUsername(exchangeHistory.getExchangeRequest().getSellerItem().getOwner().getUserName());
            vn.fptu.reasbe.model.mongodb.User recipient2 = userMService.findByUsername(getBuyer(exchangeHistory.getExchangeRequest()).getUserName());

            Notification notification1 = new Notification(senderAdmin.getUserName(), recipient1.getUserName(),
                    "Your exchange request #EX" + exchangeHistory.getExchangeRequest().getId() + " is success",
                    new Date(), TypeNotification.EXCHANGE_REQUEST, recipient1.getRegistrationTokens());

            Notification notification2 = new Notification(senderAdmin.getUserName(), recipient2.getUserName(),
                    "Your exchange request #EX" + exchangeHistory.getExchangeRequest().getId() + " is success",
                    new Date(), TypeNotification.EXCHANGE_REQUEST, recipient2.getRegistrationTokens());

            notificationService.saveAndSendNotification(notification1);
            notificationService.saveAndSendNotification(notification2);
        } else {
            exchangeHistory.setStatusExchangeHistory(StatusExchangeHistory.PENDING_EVIDENCE);
        }

        exchangeHistory = exchangeHistoryRepository.save(exchangeHistory);
        return exchangeMapper.toExchangeResponse(exchangeHistory.getExchangeRequest());
    }

    @Override
    public Integer getNumberOfSuccessfulExchanges(Integer month, Integer year) {
        LocalDateTime startDate = DateUtils.getStartOfSpecificMonth(month, year);
        LocalDateTime endDate = DateUtils.getEndOfSpecificMonth(month, year);
        return exchangeHistoryRepository.countByCreationDateBetweenAndStatusExchangeHistoryAndStatusEntity(startDate, endDate, StatusExchangeHistory.SUCCESSFUL, StatusEntity.ACTIVE);
    }

    @Override
    public Integer getNumberOfSuccessfulExchangesOfUser(Integer month, Integer year) {
        User user = getCurrentUser();
        return exchangeHistoryRepository.getNumberOfSuccessfulExchangesOfUser(month, year, user.getId());
    }

    @Override
    public BigDecimal getRevenueOfUserInOneYearFromExchanges(Integer year) {
        User user = getCurrentUser();
        return exchangeHistoryRepository.getRevenueOfUserInOneYearFromExchanges(year, user.getId());
    }

    @Override
    public Map<Integer, BigDecimal> getMonthlyRevenueOfUserInOneYearFromExchanges(Integer year) {
        User user = getCurrentUser();
        return exchangeHistoryRepository.getMonthlyRevenueOfUserInOneYearFromExchanges(year, user.getId());
    }

    @Scheduled(cron = "0 0 0,18 * * *", zone = "Asia/Ho_Chi_Minh")
    public void checkEvidenceForExchange() {
        LocalDateTime threeDaysAgo = DateUtils.getCurrentDateTime().minusDays(3);

        List<ExchangeRequest> notExchangedExchanges = exchangeRequestRepository.findAllExceedingDateExchanges(threeDaysAgo, StatusExchangeHistory.NOT_YET_EXCHANGE);
        List<ExchangeRequest> pendingEvidenceExchanges = exchangeRequestRepository.findAllExceedingDateExchanges(threeDaysAgo, StatusExchangeHistory.PENDING_EVIDENCE);
        List<ExchangeRequest> pendingExchanges = exchangeRequestRepository.findAllByStatusExchangeRequestAndExchangeDateAfter(StatusExchangeRequest.PENDING, DateUtils.getCurrentDateTime());

        //check if exchange got any report from one of each side -> CANCELLED -> if not then SUCCESSFUL
        notExchangedExchangesCronJob(notExchangedExchanges);

        //check if evidence added, if both sides added then SUCCESSFUL
        pendingEvidenceExchangesCronJob(pendingEvidenceExchanges);

        //After exchangeDate, if no approval then CANCELLED
        pendingExchangeRequestsCronJob(pendingExchanges);
    }

    private ExchangeRequest cancelExchangeRequest(ExchangeRequest request) {
        request.setStatusExchangeRequest(StatusExchangeRequest.CANCELLED);
        return exchangeRequestRepository.save(request);
    }

    private ExchangeRequest cancelApprovedExchange(ExchangeRequest request) {
        if (request.getExchangeDate().isBefore(DateUtils.getCurrentDateTime())) {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.exchangeDateBeforeNow");
        }
        if (request.getExchangeHistory() == null) {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.exchangeHistoryNull");
        }

        request.getExchangeHistory().setStatusExchangeHistory(StatusExchangeHistory.FAILED);
        request.getSellerItem().setStatusItem(StatusItem.AVAILABLE);
        if (request.getBuyerItem() != null) {
            request.getBuyerItem().setStatusItem(StatusItem.AVAILABLE);
        }

        List<Item> items = new ArrayList<>();
        items.add(request.getSellerItem());
        if (request.getBuyerItem() != null) {
            items.add(request.getBuyerItem());
        }
        vectorStoreService.addNewItem(items);

        ExchangeRequest exchangeRequest = exchangeRequestRepository.save(request);

        sendNotificationToRelatedCancelledExchangeRequestBuyer(request, exchangeRequest);
        return exchangeRequest;
    }

    private void sendNotificationToRelatedCancelledExchangeRequestBuyer(ExchangeRequest request, ExchangeRequest exchangeRequest) {
        // get old exchange request with owner item that got cancelled
        List<ExchangeRequest> oldExchangeRequests = exchangeRequestRepository.findRelatedCancelledExchangeRequests(request.getSellerItem(), exchangeRequest.getLastModificationDate());

        // check if their item is still AVAILABLE, send notification to them
        vn.fptu.reasbe.model.mongodb.User sender = userMService.getAdmin();
        Item sellerItem = exchangeRequest.getSellerItem();

        for (ExchangeRequest oldRequest : oldExchangeRequests) {
            if (Objects.isNull(oldRequest.getBuyerItem()) || oldRequest.getBuyerItem().getStatusItem().equals(StatusItem.AVAILABLE)) {
                vn.fptu.reasbe.model.mongodb.User recipient = userMService.findByUsername(getBuyer(oldRequest).getUserName());
                Notification notification = new Notification(sender.getUserName(), recipient.getUserName(),
                        sellerItem.getItemName() + " is now available for exchange. Click here to re-create the exchange request #EX" + oldRequest.getId(),
                        new Date(), TypeNotification.EXCHANGE_REQUEST, recipient.getRegistrationTokens());
                notificationService.saveAndSendNotification(notification);
            }
        }
    }

    private ExchangeRequest getExchangeRequestById(Integer id) {
        return exchangeRequestRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("ExchangeRequest", "Id", id));
    }

    private void validateExchangeRequest(ExchangeRequestRequest exchangeRequestRequest) {
        if (exchangeRequestRequest.getExchangeDate().isBefore(DateUtils.getCurrentDateTime()) ||
                exchangeRequestRequest.getExchangeDate().isEqual(DateUtils.getCurrentDateTime())) {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.exchangeDateBeforeNow");
        }
        if (exchangeRequestRequest.getSellerItemId().equals(exchangeRequestRequest.getBuyerItemId())) {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.duplicateSellerAndBuyerItem");
        }
    }

    private void pendingExchangeRequestsCronJob(List<ExchangeRequest> pendingExchanges) {
        if (!pendingExchanges.isEmpty()) {
            pendingExchanges.forEach(request -> {
                request.setStatusExchangeRequest(StatusExchangeRequest.CANCELLED);

                vn.fptu.reasbe.model.mongodb.User sender = userMService.getAdmin();

                vn.fptu.reasbe.model.mongodb.User recipient1 = userMService.findByUsername(request.getSellerItem().getOwner().getUserName());
                vn.fptu.reasbe.model.mongodb.User recipient2 = userMService.findByUsername(getBuyer(request).getUserName());

                Notification notification1 = new Notification(sender.getUserName(), recipient1.getUserName(),
                        "Your exchange request #EX" + request.getId() + " has been cancelled",
                        new Date(), TypeNotification.EXCHANGE_REQUEST, recipient1.getRegistrationTokens());

                Notification notification2 = new Notification(sender.getUserName(), recipient2.getUserName(),
                        "Your exchange request #EX" + request.getId() + " has been cancelled",
                        new Date(), TypeNotification.EXCHANGE_REQUEST, recipient2.getRegistrationTokens());

                notificationService.saveAndSendNotification(notification1);
                notificationService.saveAndSendNotification(notification2);
            });
            exchangeRequestRepository.saveAll(pendingExchanges);
            log.info("Updated {} pending exchange request(s) to CANCELLED.", pendingExchanges.size());
        } else {
            log.info("No pending exchanges found.");
        }
    }

    private void pendingEvidenceExchangesCronJob(List<ExchangeRequest> pendingEvidenceExchanges) {
        if (!pendingEvidenceExchanges.isEmpty()) {
            pendingEvidenceExchanges.forEach(request -> {
                request.getExchangeHistory().setStatusExchangeHistory(StatusExchangeHistory.SUCCESSFUL);
                request.getSellerItem().setStatusItem(StatusItem.EXCHANGED);
                if (request.getBuyerItem() != null) {
                    request.getBuyerItem().setStatusItem(StatusItem.EXCHANGED);
                }

                vn.fptu.reasbe.model.mongodb.User sender = userMService.getAdmin();

                vn.fptu.reasbe.model.mongodb.User recipient1 = userMService.findByUsername(request.getSellerItem().getOwner().getUserName());
                vn.fptu.reasbe.model.mongodb.User recipient2 = userMService.findByUsername(getBuyer(request).getUserName());

                Notification notification1 = new Notification(sender.getUserName(), recipient1.getUserName(),
                        "Your exchange request #EX" + request.getId() + " is success",
                        new Date(), TypeNotification.EXCHANGE_REQUEST, recipient1.getRegistrationTokens());

                Notification notification2 = new Notification(sender.getUserName(), recipient2.getUserName(),
                        "Your exchange request #EX" + request.getId() + " is success",
                        new Date(), TypeNotification.EXCHANGE_REQUEST, recipient2.getRegistrationTokens());

                notificationService.saveAndSendNotification(notification1);
                notificationService.saveAndSendNotification(notification2);
            });
            exchangeRequestRepository.saveAll(pendingEvidenceExchanges);
            log.info("Updated {} pending evidence exchange request(s) to SUCCESSFUL.", pendingEvidenceExchanges.size());
        } else {
            log.info("No pending evidence exchanges found.");
        }
    }

    private void notExchangedExchangesCronJob(List<ExchangeRequest> notExchangedExchanges) {
        if (!notExchangedExchanges.isEmpty()) {
            notExchangedExchanges.forEach(request -> {
                request.getExchangeHistory().setStatusExchangeHistory(StatusExchangeHistory.SUCCESSFUL);
                request.getSellerItem().setStatusItem(StatusItem.EXCHANGED);
                if (request.getBuyerItem() != null) {
                    request.getBuyerItem().setStatusItem(StatusItem.EXCHANGED);
                }

                vn.fptu.reasbe.model.mongodb.User sender = userMService.getAdmin();

                vn.fptu.reasbe.model.mongodb.User recipient1 = userMService.findByUsername(request.getSellerItem().getOwner().getUserName());
                vn.fptu.reasbe.model.mongodb.User recipient2 = userMService.findByUsername(getBuyer(request).getUserName());

                Notification notification1 = new Notification(sender.getUserName(), recipient1.getUserName(),
                        "Your exchange request #EX" + request.getId() + " is success",
                        new Date(), TypeNotification.EXCHANGE_REQUEST, recipient1.getRegistrationTokens());

                Notification notification2 = new Notification(sender.getUserName(), recipient2.getUserName(),
                        "Your exchange request #EX" + request.getId() + " is success",
                        new Date(), TypeNotification.EXCHANGE_REQUEST, recipient2.getRegistrationTokens());

                notificationService.saveAndSendNotification(notification1);
                notificationService.saveAndSendNotification(notification2);
            });
            exchangeRequestRepository.saveAll(notExchangedExchanges);
            log.info("Updated {} not exchanged exchange request(s) to SUCCESSFUL.", notExchangedExchanges.size());
        } else {
            log.info("No not exchanged exchanges found.");
        }
    }

    private void checkIfExchangeIsPending(ExchangeRequest request) {
        if (!request.getStatusExchangeRequest().equals(StatusExchangeRequest.PENDING)) {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.exchangeRequestNotPending");
        }
    }

    private void cancelOtherExchangeRequests(Item sellerItem, Item buyerItem, Integer currentRequestId) {
        List<ExchangeRequest> requests = exchangeRequestRepository
                .findAllByStatusAndSellerItemOrBuyerItem(StatusExchangeRequest.PENDING, sellerItem, buyerItem);

        // Send notification
        User currentUser = getCurrentUser();
        vn.fptu.reasbe.model.mongodb.User sender = userMService.findByUsername(currentUser.getUserName());

        for (ExchangeRequest relatedRequest : requests) {
            if (relatedRequest.getId().equals(currentRequestId)) {
                continue;
            }

            relatedRequest.setStatusExchangeRequest(StatusExchangeRequest.CANCELLED);

            vn.fptu.reasbe.model.mongodb.User recipient = userMService.findByUsername(getBuyer(relatedRequest).getUserName());
            Notification notification = new Notification(sender.getUserName(), recipient.getUserName(),
                    "Your exchange request #EX" + relatedRequest.getId() + " has been cancelled",
                    new Date(), TypeNotification.EXCHANGE_REQUEST, recipient.getRegistrationTokens());
            notificationService.saveAndSendNotification(notification);
        }

        exchangeRequestRepository.saveAll(requests);
    }

    private User getBuyer(ExchangeRequest request) {
        return request.getBuyerItem() != null ? request.getBuyerItem().getOwner() : request.getPaidBy();
    }
}
