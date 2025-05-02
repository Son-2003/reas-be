package vn.fptu.reasbe.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.fptu.reasbe.model.entity.User;
import vn.fptu.reasbe.model.entity.UserLocation;
import vn.fptu.reasbe.model.enums.core.StatusEntity;

import java.util.List;
import java.util.Optional;

public interface UserLocationRepository extends JpaRepository<UserLocation, Integer> {
    Optional<UserLocation> findByIsPrimaryTrueAndUserAndStatusEntity(User user, StatusEntity status);

    List<UserLocation> findAllByPointNull();

    Page<UserLocation> findAllByUserAndStatusEntity(User user, StatusEntity statusEntity, Pageable pageable);

    Optional<UserLocation> findByIdAndUserAndStatusEntity(Integer id, User user, StatusEntity statusEntity);
}
