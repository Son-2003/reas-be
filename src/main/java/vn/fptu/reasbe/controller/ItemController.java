package vn.fptu.reasbe.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import vn.fptu.reasbe.model.constant.AppConstants;
import vn.fptu.reasbe.model.dto.core.BaseSearchPaginationResponse;
import vn.fptu.reasbe.model.dto.item.*;
import vn.fptu.reasbe.model.enums.item.StatusItem;
import vn.fptu.reasbe.service.ItemService;
import vn.fptu.reasbe.utils.mapper.ItemMapper;

import java.util.List;

/**
 * @author ntig
 */
@RestController
@RequestMapping("/api/v1/item")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final ItemMapper itemMapper;

    @PostMapping("/search")
    public ResponseEntity<BaseSearchPaginationResponse<SearchItemResponse>> searchItemPagination(
            @RequestParam(name = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(name = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDir,
            @RequestBody @Nullable SearchItemRequest request
    ) {
        return ResponseEntity.ok(itemService.searchItemPagination(pageNo, pageSize, sortBy, sortDir, request));
    }

    @GetMapping("/user")
    public ResponseEntity<BaseSearchPaginationResponse<ItemResponse>> getAllItemOfUserByStatus(
            @RequestParam(name = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(name = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDir,
            @RequestParam(value = "userId") Integer userId,
            @RequestParam(value = "statusItem") StatusItem statusItem)
    {
        return ResponseEntity.ok(itemService.getAllItemOfUserByStatus(pageNo, pageSize, sortBy, sortDir, userId, statusItem));
    }

    @GetMapping("/current-user")
    @PreAuthorize("hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_RESIDENT)")
    public ResponseEntity<BaseSearchPaginationResponse<ItemResponse>> getAllItemOfCurrentUserByStatus(
            @RequestParam(name = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(name = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDir,
             @RequestParam(value = "statusItem") StatusItem statusItem)
    {
        return ResponseEntity.ok(itemService.getAllItemOfCurrentUserByStatusItem(pageNo, pageSize, sortBy, sortDir, statusItem));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> getItemDetail(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(itemService.getItemDetail(id));
    }

    @PreAuthorize("hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_RESIDENT)")
    @PostMapping()
    public ResponseEntity<ItemResponse> uploadItem(@Valid @RequestBody UploadItemRequest request) {
        return ResponseEntity.ok(itemMapper.toItemResponse(itemService.uploadItem(request)));
    }

    @PreAuthorize("hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_RESIDENT)")
    @PutMapping()
    public ResponseEntity<ItemResponse> updateItem(@Valid @RequestBody UpdateItemRequest request) {
        return ResponseEntity.ok(itemService.updateItem(request));
    }

    @PreAuthorize("hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_STAFF)")
    @GetMapping("/pending")
    public ResponseEntity<BaseSearchPaginationResponse<ItemResponse>> getAllPendingItem(
            @RequestParam(name = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(name = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDir
    ) {
        return ResponseEntity.ok(itemService.getAllPendingItem(pageNo, pageSize, sortBy, sortDir));
    }

    @PreAuthorize("hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_STAFF)")
    @PutMapping("/review")
    public ResponseEntity<ItemResponse> reviewItem(@RequestParam("id") Integer id, @RequestParam("statusItem") StatusItem statusItem) {
        return ResponseEntity.ok(itemService.reviewItem(id, statusItem));
    }

    @PreAuthorize("hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_RESIDENT)")
    @GetMapping("/recommend")
    public ResponseEntity<List<ItemResponse>> getRecommendedItems(
            @RequestParam(name = "id") Integer id,
            @RequestParam(name = "limit", defaultValue = AppConstants.LIST_ITEM_LIMIT, required = false) int limit
    ) {
        return ResponseEntity.ok(itemService.getRecommendedItems(id, limit));
    }

    @PreAuthorize("hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_RESIDENT)")
    @GetMapping("/exchange/recommend")
    public ResponseEntity<List<ItemResponse>> getRecommendedItemsInExchange(
            @RequestParam(name = "sellerItemId") Integer sellerItemId,
            @RequestParam(name = "limit", defaultValue = AppConstants.LIST_ITEM_LIMIT, required = false) int limit
    ) {
        return ResponseEntity.ok(itemService.getRecommendedItemsInExchange(sellerItemId, limit));
    }

    @GetMapping("/similar")
    public ResponseEntity<List<ItemResponse>> getSimilarItems(
            @RequestParam Integer itemId,
            @RequestParam(name = "limit", defaultValue = AppConstants.LIST_ITEM_LIMIT, required = false) int limit
    ) {
        return ResponseEntity.ok(itemService.getSimilarItems(itemId, limit));
    }

    @GetMapping("/others")
    public ResponseEntity<List<ItemResponse>> getOtherItemsOfUser(
            @RequestParam Integer currItemId,
            @RequestParam Integer userId,
            @RequestParam(name = "limit", defaultValue = AppConstants.LIST_ITEM_LIMIT, required = false) int limit
    ) {
        return ResponseEntity.ok(itemService.getOtherItemsOfUser(currItemId, userId, limit));
    }

    @PreAuthorize("hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_RESIDENT)")
    @PutMapping("/status")
    public ResponseEntity<ItemResponse> changeItemStatus(@RequestParam Integer itemId, @RequestParam StatusItem statusItem) {
        return ResponseEntity.ok(itemService.changeItemStatus(itemId, statusItem));
    }

    @GetMapping("/nearby")
    public ResponseEntity<BaseSearchPaginationResponse<ItemResponse>> findNearbyItems(
            @RequestParam(name = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam double distance
    ) {
        return ResponseEntity.ok(itemService.findNearbyItems(pageNo, pageSize, latitude, longitude, distance));
    }

    @GetMapping("/updated-item-in-pending-exchange")
    public ResponseEntity<Boolean> isUpdatedItemInPendingExchange(@RequestParam Integer itemId) {
        return ResponseEntity.ok(itemService.checkUpdatedItemInPendingExchange(itemId));
    }

    @PutMapping("/extend-item-for-free")
    @PreAuthorize("hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_RESIDENT)")
    public ResponseEntity<Boolean> extendItemForFree(@RequestParam Integer itemId) {
      return ResponseEntity.ok(itemService.extendItemForFree(itemId));
    }

    @PreAuthorize("hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_RESIDENT)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteItem(@PathVariable Integer id) {
        return ResponseEntity.ok(itemService.deleteItem(id));
    }

    @PreAuthorize("hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_RESIDENT)")
    @GetMapping("/check-upload-item-reach-limit")
    public ResponseEntity<Boolean> isReachMaxOfUploadItemThisMonth() {
        return ResponseEntity.ok(itemService.isReachMaxOfUploadItemThisMonth());
    }

    @PreAuthorize("hasRole(T(vn.fptu.reasbe.model.constant.AppConstants).ROLE_RESIDENT)")
    @GetMapping("/check-availability-of-seller-item")
    public ResponseEntity<Boolean> isSellerItemStillAvailable(Integer exchangeRequestId) {
        return ResponseEntity.ok(itemService.isSellerItemStillAvailable(exchangeRequestId));
    }
}
