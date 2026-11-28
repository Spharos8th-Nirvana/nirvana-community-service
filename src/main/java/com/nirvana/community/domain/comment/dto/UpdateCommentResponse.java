package com.nirvana.community.domain.comment.dto;

import java.util.UUID;

import com.nirvana.community.domain.comment.entity.Comment;

// 수정한 댓글의 식별자와 바뀐 내용. 내부 id는 주지 않는다. (COM-NFR-01)
public record UpdateCommentResponse(
	UUID commentUuid,
	String content
) {

	public static UpdateCommentResponse from(Comment comment) {
		return new UpdateCommentResponse(comment.getUuid(), comment.getContent());
	}
}
