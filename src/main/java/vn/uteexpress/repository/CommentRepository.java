package vn.uteexpress.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.uteexpress.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {

	List<Comment> findByProductIdOrderByCreatedAtDesc(Long productId);

	List<Comment> findByUserIdOrderByCreatedAtDesc(Long userId);

	Optional<Comment> findByIdAndUserId(Long id, Long userId);
}