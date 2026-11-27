package com.nirvana.community.domain.comment.dto;

import java.util.UUID;

import org.jspecify.annotations.Nullable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 어느 글의 댓글인지는 경로의 postUuid로 정한다. 작성자 값은 받지 않는다. 작성자는 Gateway 헤더의 요청자다. (COM-BRD-02)
public record CreateCommentRequest(

	// 일반 텍스트로 그대로 저장한다. (COM-NFR-04)
	@NotBlank(message = "댓글 내용을 입력해주세요.")
	@Size(max = 2000, message = "댓글 내용은 2000자 이내로 입력해주세요.")
	String content,

	// 답글일 때만 보낸다. 형식이 UUID가 아니면 본문을 읽지 못해 400이다. (COM-CMT-02)
	@Nullable UUID parentCommentUuid
) {
}
