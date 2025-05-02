package vn.fptu.reasbe.service;

import vn.fptu.reasbe.model.dto.core.BaseSearchPaginationResponse;

import vn.fptu.reasbe.model.dto.item.ItemResponse;
import vn.fptu.reasbe.model.dto.item.SearchItemRequest;
import vn.fptu.reasbe.model.dto.item.SearchItemResponse;
import vn.fptu.reasbe.model.dto.item.UpdateItemRequest;
import vn.fptu.reasbe.model.dto.item.UploadItemRequest;
import vn.fptu.reasbe.model.entity.Item;
import vn.fptu.reasbe.model.entity.SubscriptionPlan;
import vn.fptu.reasbe.model.enums.item.StatusItem;

import java.util.List;

/**
 * @author ntig
 */
public interface ItemService {
    BaseSearchPaginationResponse<SearchItemResponse> searchItemPagination(int pageNo, int pageSize, String sortBy, String sortDir, SearchItemRequest request);

    Item getItemById(Integer id);

    ItemResponse getItemDetail(Integer id);

    Item uploadItem(UploadItemRequest request);

    BaseSearchPaginationResponse<ItemResponse> getAllItemOfUserByStatus(int pageNo, int pageSize, String sortBy, String sortDir, Integer userId, StatusItem statusItem);

    BaseSearchPaginationResponse<ItemResponse> getAllItemOfCurrentUserByStatusItem(int pageNo, int pageSize, String sortBy, String sortDir, StatusItem statusItem);

    ItemResponse updateItem(UpdateItemRequest request);

    BaseSearchPaginationResponse<ItemResponse> getAllPendingItem(int pageNo, int pageSize, String sortBy, String sortDir);

    ItemResponse reviewItem(Integer id, StatusItem status);

    List<ItemResponse> getRecommendedItems(Integer itemId, int limit);

    List<ItemResponse> getRecommendedItemsInExchange(Integer itemId, int limit);

    List<ItemResponse> getSimilarItems(Integer itemId, int limit);

    List<ItemResponse> getOtherItemsOfUser(Integer itemId, Integer userId, int limit);

    ItemResponse changeItemStatus(Integer itemId, StatusItem status);

    boolean isItemExistedAndExpired(Integer itemId);

    void extendItem(Item item, SubscriptionPlan plan);

    Boolean extendItemForFree(Integer itemId);

    BaseSearchPaginationResponse<ItemResponse> findNearbyItems(int pageNo, int pageSize, double latitude, double longitude, double distance);

    Boolean deleteItem(Integer id);

    Boolean isReachMaxOfUploadItemThisMonth();

    Boolean checkUpdatedItemInPendingExchange(Integer itemId);

    Boolean isSellerItemStillAvailable(Integer exchangeRequestId);
}
