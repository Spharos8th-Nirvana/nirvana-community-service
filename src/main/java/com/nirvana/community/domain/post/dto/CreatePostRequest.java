package com.nirvana.community.domain.post.dto;

import java.util.UUID;

import org.jspecify.annotations.Nullable;

import com.nirvana.community.domain.post.entity.PostStatus;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 어느 게시판의 글인지는 경로의 boardCode로 정한다.
// 작성자 값은 받지 않는다. 작성자는 Gateway 헤더의 요청자 정보에서 온다. (COM-BRD-02)
public record CreatePostRequest(

	@NotBlank(message = "제목을 입력해주세요.")
	@Size(max = 200, message = "제목은 200자 이내로 입력해주세요.")
	String title,

	@NotBlank(message = "본문을 입력해주세요.")
	String content,

	// 카테고리를 쓰는 게시판에서만 보낸다. 형식이 UUID가 아니면 본문을 읽지 못해 400이다. (COM-PST-02)
	@Nullable UUID categoryUuid,

	// 작성할 때 고르는 상태. DRAFT(임시저장) 또는 PUBLISHED(바로 공개)만 받고, 보내지 않으면 PUBLISHED다. (COM-PST-05)
	// 예약(SCHEDULED)은 아직 만들지 않아 받지 않는다(COM-PST-06). 없는 값이면 본문을 읽지 못해 400이다.
	@Nullable PostStatus status
) {

	@AssertTrue(message = "작성할 때는 DRAFT 또는 PUBLISHED만 고를 수 있습니다.")
	public boolean isStatusWritable() {
		return status == null || status == PostStatus.DRAFT || status == PostStatus.PUBLISHED;
	}

	// 보내지 않으면 지금처럼 바로 공개한다.
	public PostStatus statusOrDefault() {
		return status == null ? PostStatus.PUBLISHED : status;
	}
}
