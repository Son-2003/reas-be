package vn.fptu.reasbe.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.fptu.reasbe.model.entity.Feedback;
import vn.fptu.reasbe.model.entity.User;
import vn.fptu.reasbe.model.enums.core.StatusEntity;

import java.util.List;
import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {
    Page<Feedback> getAllByItemOwnerAndStatusEntityIn(User user, List<StatusEntity> statusEntity, Pageable pageable);
    Page<Feedback> getAllByItemOwnerAndRatingAndStatusEntityIn(User user, Integer rating, List<StatusEntity> statusEntity, Pageable pageable);
    Optional<Feedback> findByIdAndStatusEntity(Integer id, StatusEntity statusEntity);
}
