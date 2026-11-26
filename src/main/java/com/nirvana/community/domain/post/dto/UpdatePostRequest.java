package com.nirvana.community.domain.post.dto;

import java.util.UUID;

import org.jspecify.annotations.Nullable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 고칠 글은 경로의 postUuid로 정한다. 제목·본문·카테고리를 모두 보낸다(작성과 같은 규칙). (COM-PST-08, COM-PST-01)
// 작성자·수정자 값은 받지 않는다. 수정한 사람은 Gateway 헤더의 요청자 정보에서 온다. (COM-BRD-02)
public record UpdatePostRequest(

	@NotBlank(message = "제목을 입력해주세요.")
	@Size(max = 200, message = "제목은 200자 이내로 입력해주세요.")
	String title,

	@NotBlank(message = "본문을 입력해주세요.")
	String content,

	// 카테고리를 쓰는 게시판에서만 보낸다. 형식이 UUID가 아니면 본문을 읽지 못해 400이다. (COM-PST-02)
	@Nullable UUID categoryUuid
) {
}
