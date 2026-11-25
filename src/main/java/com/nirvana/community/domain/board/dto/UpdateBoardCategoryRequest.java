package com.nirvana.community.domain.board.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// 이름과 순서만 바꾼다. 게시판을 옮기는 수정은 없다. (COM-BRD-21)
public record UpdateBoardCategoryRequest(

	@NotBlank(message = "카테고리 이름을 입력해주세요.")
	@Size(max = 100, message = "카테고리 이름은 100자 이내로 입력해주세요.")
	String name,

	@NotNull(message = "카테고리 순서를 입력해주세요.")
	@Min(value = 0, message = "카테고리 순서는 0 이상이어야 합니다.")
	Integer sortOrder
) {
}
