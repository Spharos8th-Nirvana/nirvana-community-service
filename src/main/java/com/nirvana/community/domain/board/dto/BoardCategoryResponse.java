package com.nirvana.community.domain.board.dto;

import java.util.UUID;

import com.nirvana.community.domain.board.entity.BoardCategory;

// 사용자용 카테고리 목록 항목. 사용 중인 것만 주므로 active는 넣지 않는다. 내부 id는 넣지 않는다. (COM-BRD-20, COM-NFR-01)
public record BoardCategoryResponse(
	UUID categoryUuid,
	String name,
	int sortOrder
) {

	public static BoardCategoryResponse from(BoardCategory category) {
		return new BoardCategoryResponse(category.getUuid(), category.getName(), category.getSortOrder());
	}
}
