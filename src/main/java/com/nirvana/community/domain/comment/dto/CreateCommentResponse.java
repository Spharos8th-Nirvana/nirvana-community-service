package com.nirvana.community.domain.comment.dto;

import java.util.UUID;

import com.nirvana.community.domain.comment.entity.Comment;

// 만든 댓글의 식별자와 기본 정보. 내부 id는 주지 않는다. (COM-NFR-01)
public record CreateCommentResponse(
	UUID commentUuid,
	String content,
	String authorName,
	int depth
) {

	public static CreateCommentResponse from(Comment comment) {
		return new CreateCommentResponse(
			comment.getUuid(), comment.getContent(), comment.getAuthorName(), comment.getDepth());
	}
}
