package com.nirvana.community.domain.comment.service;

import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nirvana.community.domain.board.entity.BoardType;
import com.nirvana.community.domain.board.service.BoardPermissionChecker;
import com.nirvana.community.domain.board.service.BoardReader;
import com.nirvana.community.domain.comment.entity.Comment;
import com.nirvana.community.domain.comment.repository.CommentRepository;
import com.nirvana.community.domain.post.entity.Post;
import com.nirvana.community.domain.post.service.PostQueryService;
import com.nirvana.community.global.auth.Requester;
import com.nirvana.community.global.exception.BusinessException;
import com.nirvana.community.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

// 댓글 작성 중 DB 트랜잭션이 필요한 부분(확인과 저장)만 맡는다. CommentService가 작성자 이름을 구한 뒤 부른다.
// CommentService 안의 메서드로 두면 같은 클래스 안 호출이라 @Transactional이 걸리지 않아(self-invocation) 클래스를 나눴다.
@Component
@RequiredArgsConstructor
public class CommentWriter {

	private final CommentRepository commentRepository;
	private final PostQueryService postQueryService;
	private final BoardReader boardReader;
	private final BoardPermissionChecker boardPermissionChecker;

	// 글 확인(404, 409) → 게시판 확인(비활성 404) → 댓글 허용 확인 → 작성 권한 확인 → 원댓글 또는 답글 생성 → 저장.
	// (COM-CMT-01, COM-CMT-02, COM-BRD-01)
	@Transactional
	public Comment create(UUID postUuid, Requester author, String authorName, String content,
		@Nullable UUID parentCommentUuid) {
		Post post = postQueryService.getCommentablePost(postUuid, author);
		BoardType boardType = boardReader.getActiveBoardTypeById(post.getTypeId());
		if (!boardType.isAllowComment()) {
			throw new BusinessException(ErrorCode.COMMENT_NOT_ALLOWED);
		}
		boardPermissionChecker.validateWritable(boardType.getId(), author);

		Comment comment = parentCommentUuid == null
			? Comment.create(post.getId(), author, authorName, content)
			: getParent(parentCommentUuid).reply(post.getId(), author, authorName, content);
		return commentRepository.save(comment);
	}

	// 없는 부모 댓글은 다른 글의 댓글과 같이 INVALID_PARENT_COMMENT(400)로 거절한다. 본문으로 받은 값이 잘못된 것이라서다.
	private Comment getParent(UUID parentCommentUuid) {
		return commentRepository.findByUuid(parentCommentUuid)
			.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_PARENT_COMMENT));
	}
}
