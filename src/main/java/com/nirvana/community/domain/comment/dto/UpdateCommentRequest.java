package com.nirvana.community.domain.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 바꿀 내용만 받는다. 작성과 같이 일반 텍스트로 그대로 저장한다. (COM-CMT-04, COM-NFR-04)
public record UpdateCommentRequest(

	@NotBlank(message = "댓글 내용을 입력해주세요.")
	@Size(max = 2000, message = "댓글 내용은 2000자 이내로 입력해주세요.")
	String content
) {
}
