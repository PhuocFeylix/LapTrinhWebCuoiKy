package vn.uteexpress.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.uteexpress.entity.CommentMedia;

public interface CommentMediaRepository extends JpaRepository<CommentMedia, Long> {

	List<CommentMedia> findByCommentId(Long commentId);

	void deleteByCommentId(Long commentId);
}