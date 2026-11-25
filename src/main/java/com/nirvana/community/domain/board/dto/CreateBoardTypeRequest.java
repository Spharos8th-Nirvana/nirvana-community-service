package com.nirvana.community.domain.board.dto;

import com.nirvana.community.domain.board.entity.BoardFeatures;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateBoardTypeRequest(

	@NotBlank(message = "게시판 코드를 입력해주세요.")
	@Size(max = 30, message = "게시판 코드는 30자 이내로 입력해주세요.")
	@Pattern(regexp = "^[A-Z][A-Z0-9_]*$", message = "게시판 코드는 영문 대문자, 숫자, 밑줄만 쓸 수 있습니다.")
	String code,

	@NotBlank(message = "게시판 이름을 입력해주세요.")
	@Size(max = 50, message = "게시판 이름은 50자 이내로 입력해주세요.")
	String name,

	@NotNull(message = "고정 순서 정렬 사용 여부를 입력해주세요.")
	Boolean canSortOrder,

	@NotNull(message = "댓글 사용 여부를 입력해주세요.")
	Boolean allowComment,

	@NotNull(message = "좋아요 사용 여부를 입력해주세요.")
	Boolean allowLike,

	@NotNull(message = "이미지 사용 여부를 입력해주세요.")
	Boolean allowImage,

	@NotNull(message = "예약 공개 사용 여부를 입력해주세요.")
	Boolean allowScheduledPublish,

	@NotNull(message = "카테고리 사용 여부를 입력해주세요.")
	Boolean useCategory,

	@NotNull(message = "상단 고정 사용 여부를 입력해주세요.")
	Boolean allowPin,

	// 0~10만 받는다. (COM-BRD-14)
	@NotNull(message = "상단 고정 최대 개수를 입력해주세요.")
	@Min(value = 0, message = "상단 고정 최대 개수는 0 이상이어야 합니다.")
	@Max(value = 10, message = "상단 고정 최대 개수는 10 이하여야 합니다.")
	Integer maxPinCount
) {

	public BoardFeatures toFeatures() {
		return new BoardFeatures(canSortOrder, allowComment, allowLike, allowImage,
			allowScheduledPublish, useCategory, allowPin, maxPinCount);
	}
}
