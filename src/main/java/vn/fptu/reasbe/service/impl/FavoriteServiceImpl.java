package vn.fptu.reasbe.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import vn.fptu.reasbe.model.dto.core.BaseSearchPaginationResponse;
import vn.fptu.reasbe.model.dto.favorite.FavoriteResponse;
import vn.fptu.reasbe.model.entity.Favorite;
import vn.fptu.reasbe.model.entity.Item;
import vn.fptu.reasbe.model.entity.User;
import vn.fptu.reasbe.model.enums.item.StatusItem;
import vn.fptu.reasbe.model.exception.ReasApiException;
import vn.fptu.reasbe.repository.FavoriteRepository;
import vn.fptu.reasbe.service.AuthService;
import vn.fptu.reasbe.service.FavoriteService;
import vn.fptu.reasbe.service.ItemService;
import vn.fptu.reasbe.utils.mapper.FavoriteMapper;
import vn.fptu.reasbe.utils.mapper.ItemMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static vn.fptu.reasbe.model.dto.core.BaseSearchPaginationResponse.getPageable;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final AuthService authService;
    private final ItemService itemService;
    private final FavoriteRepository favoriteRepository;
    private final FavoriteMapper favoriteMapper;
    private final ItemMapper itemMapper;

    @Override
    public BaseSearchPaginationResponse<FavoriteResponse> getAllFavoriteItems(int pageNo, int pageSize, String sortBy, String sortDir) {
        User user = authService.getCurrentUser();

        return BaseSearchPaginationResponse.of(favoriteRepository.findAllByUser(user, getPageable(pageNo, pageSize, sortBy, sortDir))
                .map(fav -> favoriteMapper.toResponseWithFavorite(fav, getFavIds(user), itemMapper)));
    }

    @Override
    public FavoriteResponse addToFavorite(Integer itemId) {
        User user = authService.getCurrentUser();

        Item item = itemService.getItemById(itemId);

        if (!item.getStatusItem().equals(StatusItem.AVAILABLE)) {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.cannotAddToFavorite");
        }

        if (Objects.equals(item.getOwner(), user)) {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.cannotAddYourOwnItemToFavorite");
        }

        if (user.getFavorites() != null && user.getFavorites().stream().anyMatch(favorite -> favorite.getItem().equals(item))) {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.itemExistsInFavorite");
        }

        Favorite favoriteItem = Favorite.builder()
                .item(item)
                .user(user)
                .build();

        return favoriteMapper.toResponseWithFavorite(favoriteRepository.save(favoriteItem), getFavIds(user), itemMapper);
    }

    @Override
    public Boolean deleteFromFavorite(Integer itemId) {
        User user = authService.getCurrentUser();

        Favorite favorite = favoriteRepository.findByItemId(itemId)
                .orElseThrow(() -> new ReasApiException(HttpStatus.BAD_REQUEST, "error.itemNotExistsInFavorite"));

        if (favorite.getUser() == null || !favorite.getUser().equals(user)) {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.invalidUser");
        }

        favoriteRepository.delete(favorite);

        return true ;
    }

    public List<Integer> getFavIds(User user) {
        if (user.getFavorites() != null && !user.getFavorites().isEmpty()) {
            return user.getFavorites().stream()
                    .map(fav -> fav.getItem().getId())
                    .toList();
        } else {
            return new ArrayList<>();
        }
    }
}
