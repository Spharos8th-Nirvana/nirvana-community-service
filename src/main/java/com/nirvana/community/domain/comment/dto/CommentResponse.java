package com.nirvana.community.domain.comment.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import com.nirvana.community.domain.comment.entity.Comment;
import com.nirvana.community.global.auth.AccountType;
import com.nirvana.community.global.auth.Requester;

// 댓글 목록의 한 항목. 답글은 replies에 작성순으로 담긴다(깊이 2 댓글의 replies는 항상 빈 목록). 내부 id는 넣지 않는다. (COM-CMT-03, COM-NFR-01)
// 삭제·숨김 댓글은 자리만 남아 content가 null이고 deleted·hidden으로 구분한다. 숨김 댓글은 ADMIN과 작성자 본인에게만 내용을 준다. (COM-CMT-06)
// authorName은 작성 당시 이름이다(현재 닉네임 반영은 COM-INT-01 이벤트에서 한다).
public record CommentResponse(
	UUID commentUuid,
	@Nullable String content,
	String authorName,
	AccountType authorType,
	LocalDateTime createdAt,
	LocalDateTime updatedAt,
	int depth,
	boolean deleted,
	boolean hidden,
	boolean mine,
	long likeCount,
	boolean likedByMe,
	List<CommentResponse> replies
) {

	// 댓글 좋아요(COM-LIK-03)가 아직 없어 likeCount는 0, likedByMe는 false다. 기능이 생기면 실제 값으로 바꾼다.
	public static CommentResponse of(Comment comment, List<CommentResponse> replies, @Nullable Requester requester) {
		return new CommentResponse(
			comment.getUuid(),
			comment.isContentViewableBy(requester) ? comment.getContent() : null,
			comment.getAuthorName(),
			comment.getAuthorType(),
			comment.getCreatedAt(),
			comment.getUpdatedAt(),
			comment.getDepth(),
			comment.isDeleted(),
			comment.isHidden(),
			comment.isWrittenBy(requester),
			0L,
			false,
			replies);
	}
}
