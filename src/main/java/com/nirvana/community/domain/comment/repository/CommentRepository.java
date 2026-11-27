package com.nirvana.community.domain.comment.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nirvana.community.domain.comment.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {

	// 단건 조회: 답글 작성에서 부모 댓글을 읽는다. 삭제·숨김과 관계없이 읽고, 답글을 달 수 있는지는 Comment가 판단한다. (COM-CMT-02)
	Optional<Comment> findByUuid(UUID uuid);
}
