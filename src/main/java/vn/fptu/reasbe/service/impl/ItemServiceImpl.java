package vn.fptu.reasbe.service.impl;

import lombok.extern.slf4j.Slf4j;

import org.apache.logging.log4j.util.Strings;
import org.locationtech.jts.geom.Point;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import org.springframework.web.client.RestTemplate;

import vn.fptu.reasbe.model.constant.AppConstants;
import vn.fptu.reasbe.model.dto.core.BaseSearchPaginationResponse;
import vn.fptu.reasbe.model.dto.desireditem.DesiredItemDto;
import vn.fptu.reasbe.model.dto.goongio.DistanceMatrixResponse;
import vn.fptu.reasbe.model.dto.item.ItemResponse;
import vn.fptu.reasbe.model.dto.item.SearchItemRequest;
import vn.fptu.reasbe.model.dto.item.SearchItemResponse;
import vn.fptu.reasbe.model.dto.item.UpdateItemRequest;
import vn.fptu.reasbe.model.dto.item.UploadItemRequest;
import vn.fptu.reasbe.model.entity.DesiredItem;
import vn.fptu.reasbe.model.entity.ExchangeRequest;
import vn.fptu.reasbe.model.entity.Item;
import vn.fptu.reasbe.model.entity.SubscriptionPlan;
import vn.fptu.reasbe.model.entity.User;
import vn.fptu.reasbe.model.entity.UserSubscription;
import vn.fptu.reasbe.model.enums.core.StatusEntity;
import vn.fptu.reasbe.model.enums.exchange.StatusExchangeRequest;
import vn.fptu.reasbe.model.enums.item.StatusItem;
import vn.fptu.reasbe.model.enums.item.TypeExchange;
import vn.fptu.reasbe.model.enums.notification.TypeNotification;
import vn.fptu.reasbe.model.exception.ReasApiException;
import vn.fptu.reasbe.model.exception.ResourceNotFoundException;
import vn.fptu.reasbe.model.mongodb.Notification;
import vn.fptu.reasbe.repository.DesiredItemRepository;
import vn.fptu.reasbe.repository.ExchangeRequestRepository;
import vn.fptu.reasbe.repository.ItemRepository;
import vn.fptu.reasbe.service.AuthService;
import vn.fptu.reasbe.service.BrandService;
import vn.fptu.reasbe.service.CategoryService;
import vn.fptu.reasbe.service.ExchangeService;
import vn.fptu.reasbe.service.ItemService;
import vn.fptu.reasbe.service.UserLocationService;
import vn.fptu.reasbe.service.SubscriptionPlanService;
import vn.fptu.reasbe.service.UserSubscriptionService;
import vn.fptu.reasbe.service.VectorStoreService;
import vn.fptu.reasbe.service.mongodb.NotificationService;
import vn.fptu.reasbe.service.mongodb.UserMService;
import vn.fptu.reasbe.utils.common.DateUtils;
import vn.fptu.reasbe.utils.common.GeometryUtils;
import vn.fptu.reasbe.utils.mapper.DesiredItemMapper;
import vn.fptu.reasbe.utils.mapper.ItemMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static vn.fptu.reasbe.model.dto.core.BaseSearchPaginationResponse.getPageable;

/**
 * @author ntig
 */
@Slf4j
@Service
@Transactional(rollbackFor = Throwable.class)
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    @Value("${goongio.config.api-key}")
    private String GOONGIO_API_KEY;

    @Value("${goongio.config.distance-matrix-url}")
    private String GOONGIO_DISTANCE_MATRIX_URL;

    private final RestTemplate restTemplate = new RestTemplate();

    private final CategoryService categoryService;
    private final BrandService brandService;
    private final AuthService authService;
    private final VectorStoreService vectorStoreService;
    private final ItemRepository itemRepository;
    private final DesiredItemRepository desiredItemRepository;
    private final UserSubscriptionService userSubscriptionService;
    private final ItemMapper itemMapper;
    private final DesiredItemMapper desiredItemMapper;
    private final NotificationService notificationService;
    private final UserMService userMService;
    private final UserLocationService userLocationService;
    private final SubscriptionPlanService subscriptionPlanService;
    private final ExchangeRequestRepository exchangeRequestRepository;

    @Override
    public BaseSearchPaginationResponse<SearchItemResponse> searchItemPagination(int pageNo, int pageSize, String sortBy, String sortDir, SearchItemRequest request) {
        return BaseSearchPaginationResponse.of(itemRepository.searchItemPagination(request, getPageable(pageNo, pageSize, sortBy, sortDir))
                .map(item -> itemMapper.toSearchItemResponse(item, getFavIds())));
    }

    @Override
    public BaseSearchPaginationResponse<ItemResponse> getAllItemOfUserByStatus(int pageNo, int pageSize, String sortBy, String sortDir, Integer userId, StatusItem statusItem) {
        if (statusItem.equals(StatusItem.AVAILABLE) || statusItem.equals(StatusItem.EXCHANGED)) {
            return BaseSearchPaginationResponse.of(getAllItemByUserIdAndStatusItem(userId, statusItem, getPageable(pageNo, pageSize, sortBy, sortDir)).map(this::mapToItemResponsesWithFavorite));
        } else {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.invalidStatusItem");
        }
    }

    @Override
    public BaseSearchPaginationResponse<ItemResponse> getAllItemOfCurrentUserByStatusItem(int pageNo, int pageSize, String sortBy, String sortDir, StatusItem statusItem) {
        User user = authService.getCurrentUser();
        return BaseSearchPaginationResponse.of(getAllItemByUserIdAndStatusItem(user.getId(), statusItem, getPageable(pageNo, pageSize, sortBy, sortDir)).map(itemMapper::toItemResponse));
    }

    @Override
    public Item getItemById(Integer id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", id));
    }

    @Override
    public ItemResponse getItemDetail(Integer id) {
        Item item = getItemById(id);
        return mapToItemResponsesWithFavorite(item);
    }

    @Override
    public Item uploadItem(UploadItemRequest request) {
        User currentUser = authService.getCurrentUser();

        if (Boolean.TRUE.equals(isReachMaxOfUploadItemThisMonth())) {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.reachMaxUploadItem");
        }

        Item newItem = itemMapper.toEntity(request);
        newItem.setOwner(currentUser);
        newItem.setCategory(categoryService.getCategoryById(request.getCategoryId()));
        newItem.setBrand(brandService.getBrandById(request.getBrandId()));
        newItem.setUserLocation(userLocationService.getUserLocationOfCurrentUserById(request.getUserLocationId()));
        newItem.setStatusItem(StatusItem.PENDING);

        if (request.getDesiredItem() != null) {
            validateDesiredItemPrice(request.getDesiredItem());
            DesiredItem newDesiredItem = desiredItemMapper.toDesiredItem(request.getDesiredItem());
            prepareDesiredItem(newDesiredItem, request.getDesiredItem());
            newItem.setTypeExchange(TypeExchange.EXCHANGE_WITH_DESIRED_ITEM);
            newItem.setDesiredItem(desiredItemRepository.save(newDesiredItem));
        } else {
            newItem.setTypeExchange(TypeExchange.OPEN_EXCHANGE);
        }
        return itemRepository.save(newItem);
    }

    @Override
    public ItemResponse updateItem(UpdateItemRequest request) {
        Item existedItem = getItemById(request.getId());
        User currentUser = authService.getCurrentUser();

        if (!existedItem.getOwner().equals(currentUser)) {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.invalidOwner");
        }

        if (!existedItem.getStatusItem().equals(StatusItem.PENDING) && !existedItem.getStatusItem().equals(StatusItem.AVAILABLE)) {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.cannotUpdateItem");
        }

        if (existedItem.getStatusItem().equals(StatusItem.AVAILABLE) &&
                Boolean.TRUE.equals(checkUpdatedItemInPendingExchange(existedItem.getId()))) {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.updatedItemExistsInExchangeRequest");
        }

        itemMapper.updateItem(existedItem, request);
        existedItem.setCategory(categoryService.getCategoryById(request.getCategoryId()));
        existedItem.setBrand(brandService.getBrandById(request.getBrandId()));
        existedItem.setUserLocation(userLocationService.getUserLocationOfCurrentUserById(request.getUserLocationId()));
        existedItem.setStatusItem(StatusItem.PENDING);

        DesiredItem existedDesiredItem = existedItem.getDesiredItem();
        DesiredItemDto desiredItemDto = request.getDesiredItem();

        if (desiredItemDto == null) {
            // If there's an existing item, remove it
            if (existedDesiredItem != null) {
                existedItem.setTypeExchange(TypeExchange.OPEN_EXCHANGE);
                desiredItemRepository.delete(existedDesiredItem);
                existedItem.setDesiredItem(null);
            }
        } else {
            // desiredItemDto is not null
            validateDesiredItemPrice(desiredItemDto);

            if (existedDesiredItem != null) {
                // Update existing desired item
                desiredItemMapper.updateDesiredItem(existedDesiredItem, desiredItemDto);
                prepareDesiredItem(existedDesiredItem, desiredItemDto);
                desiredItemRepository.save(existedDesiredItem);
            } else {
                // Create new desired item
                DesiredItem newDesiredItem = desiredItemMapper.toDesiredItem(desiredItemDto);
                prepareDesiredItem(newDesiredItem, desiredItemDto);
                existedItem.setTypeExchange(TypeExchange.EXCHANGE_WITH_DESIRED_ITEM);
                existedItem.setDesiredItem(desiredItemRepository.save(newDesiredItem));
            }
        }
        vectorStoreService.deleteItem(List.of(existedItem));

        return itemMapper.toItemResponse(itemRepository.save(existedItem));
    }

    @Override
    public BaseSearchPaginationResponse<ItemResponse> getAllPendingItem(int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        return BaseSearchPaginationResponse.of(itemRepository.findAllByStatusItemAndStatusEntity(StatusItem.PENDING, StatusEntity.ACTIVE, pageable).map(itemMapper::toItemResponse));
    }

    @Override
    public ItemResponse reviewItem(Integer id, StatusItem status) {
        Item pendingItem = getItemById(id);
        User currentUser = authService.getCurrentUser();
        vn.fptu.reasbe.model.mongodb.User sender = userMService.findByUsername(currentUser.getUserName());
        vn.fptu.reasbe.model.mongodb.User recipient = userMService.findByUsername(pendingItem.getOwner().getUserName());
        Notification notification;

        if (!pendingItem.getStatusItem().equals(StatusItem.PENDING))
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.pendingItemOnly");

        if (status.equals(StatusItem.AVAILABLE)) {
            pendingItem.setStatusItem(StatusItem.AVAILABLE);

            if (pendingItem.getExpiredTime() == null) {
                int expiredTime = userSubscriptionService.getUserCurrentSubscription() != null ? AppConstants.EXPIRED_TIME_WEEKS_PREMIUM : AppConstants.EXPIRED_TIME_WEEKS;
                pendingItem.setApprovedTime(DateUtils.getCurrentDateTime());
                pendingItem.setExpiredTime(DateUtils.toStartOfDay(pendingItem.getApprovedTime().plusWeeks(expiredTime)));
            }

            notification = new Notification(sender.getUserName(), recipient.getUserName(),
                    "Your item has been approved",
                    new Date(), TypeNotification.UPLOAD_ITEM, recipient.getRegistrationTokens());

        } else if (status.equals(StatusItem.REJECTED)) {
            pendingItem.setStatusItem(StatusItem.REJECTED);

            notification = new Notification(sender.getUserName(), recipient.getUserName(),
                    "Your item has been rejected",
                    new Date(), TypeNotification.UPLOAD_ITEM, recipient.getRegistrationTokens());
        } else {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.invalidStatusItem");
        }

        vectorStoreService.addNewItem(List.of(pendingItem));

        // Send notification
        notificationService.saveAndSendNotification(notification);
        return itemMapper.toItemResponse(itemRepository.save(pendingItem));
    }

    @Override
    public List<ItemResponse> getRecommendedItems(Integer itemId, int limit) {
        User curr = authService.getCurrentUser();

        Item item = getItemById(itemId);

        if (!item.getOwner().equals(curr)) {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.invalidOwner");
        }

        DesiredItem desiredItem = item.getDesiredItem();

        if (desiredItem == null || Strings.isBlank(desiredItem.getDescription())) {
            return Collections.emptyList();
        }

        String filter = buildFilterForRecommendedItem(desiredItem);

        List<Document> documents = vectorStoreService.searchSimilarItems(desiredItem.getDescription(), filter, limit);

        return mapToItemResponses(documents);
    }

    @Override
    public List<ItemResponse> getRecommendedItemsInExchange(Integer itemId, int limit) {
        User currBuyer = authService.getCurrentUser();

        Item sellerItem = getItemById(itemId);

        DesiredItem desiredItem = sellerItem.getDesiredItem();

        if (desiredItem == null) {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.desiredItemEmpty");
        }

        String filter = buildFilterForRecommendedItemInExchange(desiredItem, currBuyer.getId());

        List<Document> documents = vectorStoreService.searchSimilarItems(desiredItem.getDescription(), filter, limit);

        return mapToItemResponses(documents);
    }

    @Override
    public List<ItemResponse> getSimilarItems(Integer itemId, int limit) {
        Item item = getItemById(itemId);

        String itemContent = String.format("Item: %s, Brand: %s, Category: %s, Price: %s, Description: %s, Condition: %s",
                item.getItemName(),
                item.getBrand().getBrandName(),
                item.getCategory().getCategoryName(),
                item.getPrice().toString(),
                item.getDescription(),
                item.getConditionItem().getCode());

        String filter = "itemId != " + item.getId();

        List<Document> documents = vectorStoreService.searchSimilarItems(itemContent, filter, limit);

        return mapToItemResponses(documents);
    }

    @Override
    public List<ItemResponse> getOtherItemsOfUser(Integer currItemId, Integer userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return itemRepository.findByStatusItemAndStatusEntityAndOwnerIdAndIdNotOrderByApprovedTimeDesc(StatusItem.AVAILABLE, StatusEntity.ACTIVE, userId, currItemId, pageable)
                .stream()
                .map(itemMapper::toItemResponse)
                .toList();
    }

    @Override
    public ItemResponse changeItemStatus(Integer itemId, StatusItem status) {
        User user = authService.getCurrentUser();

        Item item = getItemById(itemId);

        if (!item.getOwner().equals(user)) {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.invalidOwner");
        }

        if ((status.equals(StatusItem.NO_LONGER_FOR_EXCHANGE) && !item.getStatusItem().equals(StatusItem.AVAILABLE)) ||
                (status.equals(StatusItem.AVAILABLE) && !item.getStatusItem().equals(StatusItem.NO_LONGER_FOR_EXCHANGE))) {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.invalidStatus");
        }

        item.setStatusItem(status);

        return itemMapper.toItemResponse(itemRepository.save(item));
    }

    @Override
    public boolean isItemExistedAndExpired(Integer itemId) {
        return itemRepository.existsByIdAndStatusItemEqualsAndStatusEntityEquals(itemId, StatusItem.EXPIRED, StatusEntity.ACTIVE);
    }

    @Override
    public void extendItem(Item item, SubscriptionPlan plan) {
        if (item.getStatusItem() != StatusItem.EXPIRED) {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.itemIsNotExpiredYet");
        }
        item.setStatusItem(StatusItem.AVAILABLE);
        item.setExpiredTime(DateUtils.getEndDateByStartDateAndDuration(DateUtils.getCurrentDateTime(), plan.getDuration()));
        itemRepository.save(item);
    }

    @Override
    public Boolean extendItemForFree(Integer itemId) {
        UserSubscription userSubscription = userSubscriptionService.getUserCurrentSubscription();
        if (userSubscription == null) {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.userSubscriptionNotFound");
        }
        if (userSubscription.getNumberOfFreeExtensionLeft() <= 0) {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.noExtensionLeft");
        }

        SubscriptionPlan planTypeExtension = subscriptionPlanService.getSubscriptionPlanTypeExtension();

        Item item = getItemById(itemId);
        if (item.getStatusItem() != StatusItem.EXPIRED) {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.itemIsNotExpiredYet");
        }
        item.setStatusItem(StatusItem.AVAILABLE);
        item.setExpiredTime(DateUtils.getEndDateByStartDateAndDuration(DateUtils.getCurrentDateTime(), planTypeExtension.getDuration()));
        itemRepository.save(item);

        userSubscriptionService.updateNumberOfExtensionLeft(userSubscription);

        return true;
    }

    @Override
    public BaseSearchPaginationResponse<ItemResponse> findNearbyItems(int pageNo, int pageSize, double latitude, double longitude, double distance) {
        Point refPoint = GeometryUtils.createPoint(longitude, latitude);
        double distanceInMeters = distance * 1000;

        List<Item> items = itemRepository.findNearbyItems(refPoint, distanceInMeters, StatusItem.AVAILABLE.getCode(), StatusEntity.ACTIVE.toString());

        DistanceMatrixResponse response = getDistanceMatrix(latitude, longitude, items);

        if (response.getRows().isEmpty()) {
            return BaseSearchPaginationResponse.of(Page.empty());
        }

        List<DistanceMatrixResponse.Element> elements = response.getRows().getFirst().getElements();

        //Check if elements size match item size
        if (elements.size() != items.size()) {
            return BaseSearchPaginationResponse.of(Page.empty());
        }

        // Map each item with the corresponding result from the API and keep the correct order with LinkedHashMap
        Map<Item, DistanceMatrixResponse.Element> itemDistanceMap = new LinkedHashMap<>();
        for (int i = 0; i < items.size(); i++) {
            itemDistanceMap.put(items.get(i), elements.get(i));
        }

        // filter by distance
        List<ItemResponse> filteredItems = itemDistanceMap.entrySet().stream()
                .filter(entry -> entry.getValue().getDistance().getValue() <= distanceInMeters)
                .map(entry -> {
                    ItemResponse itemResponse = itemMapper.toItemResponse(entry.getKey());
                    itemResponse.setDistance(entry.getValue().getDistance().getText());
                    return itemResponse;
                })
                .toList();

        Pageable pageable = PageRequest.of(pageNo, pageSize);
        int startIndex = (int) pageable.getOffset();
        int endIndex = Math.min(startIndex + pageable.getPageSize(), filteredItems.size());
        List<ItemResponse> pagedList = filteredItems.subList(startIndex, endIndex);
        return BaseSearchPaginationResponse.of(new PageImpl<>(pagedList, pageable, filteredItems.size()));
    }

    @Override
    public Boolean deleteItem(Integer id) {
        User user = authService.getCurrentUser();

        Item item = getItemById(id);

        if (!item.getOwner().equals(user)) {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.invalidOwner");
        }

        if (!item.getStatusItem().equals(StatusItem.NO_LONGER_FOR_EXCHANGE) && !item.getStatusItem().equals(StatusItem.PENDING)) {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.invalidStatus");
        }
        item.setStatusEntity(StatusEntity.INACTIVE);

        itemRepository.save(item);

        return true;
    }

    @Override
    public Boolean checkUpdatedItemInPendingExchange(Integer itemId) {
        return exchangeRequestRepository.existByItemAndStatus(itemId, StatusExchangeRequest.PENDING);
    }

    public DistanceMatrixResponse getDistanceMatrix(double originLat, double originLng, List<Item> items) {
        String destinations = items.stream()
                .map(item -> item.getUserLocation().getLatitude() + "," + item.getUserLocation().getLongitude())
                .collect(Collectors.joining("|"));

        // Build the URL with parameters
        String url = GOONGIO_DISTANCE_MATRIX_URL +
                "?origins=" + originLat + "," + originLng +
                "&destinations=" + destinations +
                "&vehicle=" + "car" +
                "&api_key=" + GOONGIO_API_KEY;

        //Calling API
        DistanceMatrixResponse response = restTemplate.getForObject(url, DistanceMatrixResponse.class);
        if (response == null) {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.distanceMatrixAPIResponseNull");
        }

        return response;
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Ho_Chi_Minh")
    public void checkExpiredItems() {
        List<Item> expiredItems = itemRepository.findAllByExpiredTimeBeforeAndStatusItemAndStatusEntity(DateUtils.getCurrentDateTime(), StatusItem.AVAILABLE, StatusEntity.ACTIVE);
        expiredItems.forEach(expiredItem -> {
            expiredItem.setStatusItem(StatusItem.EXPIRED);
            setExchangeRequestCancelled(expiredItem);

            // Send notification
            vn.fptu.reasbe.model.mongodb.User recipient = userMService.findByUsername(expiredItem.getOwner().getUserName());
            Notification notification = new Notification(userMService.getAdmin().getUserName(), recipient.getUserName(),
                    "Your item " + expiredItem.getItemName() + " has expired",
                    new Date(), TypeNotification.ITEM_EXPIRED, recipient.getRegistrationTokens());
            notificationService.saveAndSendNotification(notification);
        });
        itemRepository.saveAll(expiredItems);
        log.info("Updated {} expired items", expiredItems.size());
    }

    private void setExchangeRequestCancelled(Item expiredItem) {
        List<ExchangeRequest> relatedRequests = Stream.concat(
                        Optional.ofNullable(expiredItem.getSellerExchangeRequests()).stream()
                                .flatMap(List::stream),
                        Optional.ofNullable(expiredItem.getBuyerExchangeRequests()).stream()
                                .flatMap(List::stream)
                )
                .filter(req -> req.getStatusExchangeRequest() == StatusExchangeRequest.PENDING)
                .toList();

        if (!relatedRequests.isEmpty()) {
            for (ExchangeRequest request : relatedRequests) {
                request.setStatusExchangeRequest(StatusExchangeRequest.CANCELLED);

                vn.fptu.reasbe.model.mongodb.User sender = userMService.getAdmin();

                vn.fptu.reasbe.model.mongodb.User recipient1 = userMService.findByUsername(request.getSellerItem().getOwner().getUserName());
                vn.fptu.reasbe.model.mongodb.User recipient2 = userMService.findByUsername(request.getBuyerItem() != null ?
                        request.getBuyerItem().getOwner().getUserName() : request.getPaidBy().getUserName());

                Notification notification1 = new Notification(sender.getUserName(), recipient1.getUserName(),
                        "Your exchange request #EX" + request.getId() + " has been cancelled",
                        new Date(), TypeNotification.EXCHANGE_REQUEST, recipient1.getRegistrationTokens());

                Notification notification2 = new Notification(sender.getUserName(), recipient2.getUserName(),
                        "Your exchange request #EX" + request.getId() + " has been cancelled",
                        new Date(), TypeNotification.EXCHANGE_REQUEST, recipient2.getRegistrationTokens());

                notificationService.saveAndSendNotification(notification1);
                notificationService.saveAndSendNotification(notification2);
            }
            exchangeRequestRepository.saveAll(relatedRequests);
        }
    }

    @Override
    public Boolean isReachMaxOfUploadItemThisMonth() {
        User currentUser = authService.getCurrentUser();

        LocalDateTime monthStart = DateUtils.getFirstDayOfCurrentMonth();
        LocalDateTime now = DateUtils.getCurrentDateTime();

        // Count item uploaded in the current month
        List<StatusItem> countedStatuses = List.of(
                StatusItem.IN_EXCHANGE,
                StatusItem.PENDING,
                StatusItem.AVAILABLE,
                StatusItem.EXPIRED,
                StatusItem.EXCHANGED,
                StatusItem.NO_LONGER_FOR_EXCHANGE
        );

        int totalUploadedThisMonth = itemRepository
                .countByOwnerAndStatusItemInAndStatusEntityAndLastModificationDateBetween(
                        currentUser,
                        countedStatuses,
                        StatusEntity.ACTIVE,
                        monthStart,
                        now
                );

        // Get user’s most recent subscription in current month
        UserSubscription lastSub = userSubscriptionService.getUserSubscriptionInCurrentMonth();

        if (Objects.nonNull(lastSub)) {
            LocalDateTime endDate = lastSub.getEndDate();
            // still premium? endDate ≥ today
            if (!endDate.isBefore(now) && totalUploadedThisMonth >= AppConstants.MAX_ITEM_UPLOADED_PREMIUM) {
                return true;
            }

            // expired mid‐month? (endDate in [monthStart…today))
            if (!endDate.isBefore(monthStart) && endDate.isBefore(now)) {
                // Count how many were uploaded before subscription expired
                int usedBeforeExpiry = itemRepository
                        .countByOwnerAndStatusItemInAndStatusEntityAndLastModificationDateBetween(
                                currentUser,
                                countedStatuses,
                                StatusEntity.ACTIVE,
                                monthStart,
                                endDate
                        );

                // Compute how many extra uploads remain
                int premiumLeft = AppConstants.MAX_ITEM_UPLOADED_PREMIUM - usedBeforeExpiry;
                int fallbackFreeCap = AppConstants.MAX_ITEM_UPLOADED;
                int allowedAfterExpiry = Math.min(premiumLeft, fallbackFreeCap);

                // If they’ve already hit the combined allowance, block
                if (totalUploadedThisMonth >= usedBeforeExpiry + allowedAfterExpiry) {
                    return true;
                }
            }
        }

        // No subscription this month (or expired before month start)
        return totalUploadedThisMonth >= AppConstants.MAX_ITEM_UPLOADED;
    }

    @Override
    public Boolean isSellerItemStillAvailable(Integer exchangeRequestId) {
        ExchangeRequest exchangeRequest = exchangeRequestRepository.findById(exchangeRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("ExchangeRequest", "id", exchangeRequestId));
        return exchangeRequest.getSellerItem().getStatusItem() == StatusItem.AVAILABLE;
    }

    private Page<Item> getAllItemByUserIdAndStatusItem(Integer userId, StatusItem statusItem, Pageable pageable) {
        return itemRepository.findAllByOwnerIdAndStatusItemAndStatusEntityOrderByCreationDateDesc(userId, statusItem, StatusEntity.ACTIVE, pageable);
    }

    private void prepareDesiredItem(DesiredItem desiredItem, DesiredItemDto desiredItemDto) {
        desiredItem.setBrand(brandService.getBrandById(desiredItemDto.getBrandId()));
        desiredItem.setCategory(categoryService.getCategoryById(desiredItemDto.getCategoryId()));
    }

    private void validateDesiredItemPrice(DesiredItemDto dto) {
        if (Objects.nonNull(dto.getMaxPrice()) && Objects.nonNull(dto.getMinPrice()) && (dto.getMinPrice().compareTo(dto.getMaxPrice()) > 0)) {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.minPriceGreaterThanMaxPrice");
        }
    }

    private ItemResponse mapToItemResponsesWithFavorite(Item item) {
        return itemMapper.toItemResponseWithFavorite(item, getFavIds());
    }

    private List<Integer> getFavIds() {
        User user = authService.getCurrentUser();

        List<Integer> favIds = new ArrayList<>();

        if (user != null && user.getFavorites() != null && !user.getFavorites().isEmpty()) {
            favIds = user.getFavorites().stream()
                    .map(favorite -> favorite.getItem().getId())  // Extract item ID
                    .toList();
        }

        return favIds;
    }

    private List<ItemResponse> mapToItemResponses(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Integer> itemIds = documents.stream()
                .map(doc -> Integer.valueOf(doc.getMetadata().get("itemId").toString()))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<Integer, ItemResponse> itemResponseMap = itemRepository.findAllById(itemIds).stream()
                .collect(Collectors.toMap(Item::getId, this::mapToItemResponsesWithFavorite, (a, b) -> a, LinkedHashMap::new));

        return itemIds.stream()
                .map(itemResponseMap::get)
                .filter(Objects::nonNull)
                .toList();
    }

    private String buildFilterForRecommendedItemInExchange(DesiredItem desiredItem, Integer buyerId) {
        StringBuilder filter = new StringBuilder("ownerId == " + buyerId);

        return getCommonItemFilter(desiredItem, filter);
    }

    private String buildFilterForRecommendedItem(DesiredItem desiredItem) {
        StringBuilder filter = new StringBuilder("ownerId != " + desiredItem.getItem().getOwner().getId());

        return getCommonItemFilter(desiredItem, filter);
    }

    private String getCommonItemFilter(DesiredItem desiredItem, StringBuilder filter) {
        if (desiredItem.getBrand() != null) {
            filter.append(" && brandName == '").append(desiredItem.getBrand().getBrandName()).append("'");
        }
        if (desiredItem.getCategory() != null) {
            filter.append(" && categoryName == '").append(desiredItem.getCategory().getCategoryName()).append("'");
        }
        if (desiredItem.getConditionItem() != null) {
            filter.append(" && conditionItem == '").append(desiredItem.getConditionItem().getCode()).append("'");
        }
        if (desiredItem.getMinPrice() != null) {
            filter.append(" && price >= ").append(desiredItem.getMinPrice());
        }
        if (desiredItem.getMaxPrice() != null) {
            filter.append(" && price <= ").append(desiredItem.getMaxPrice());
        }

        return filter.toString();
    }
}
