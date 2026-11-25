package com.nirvana.community.domain.board.dto;

import java.util.UUID;

import com.nirvana.community.domain.board.entity.BoardCategory;

// 관리용 카테고리 정보. 보관된 것도 다시 켜거나 고칠 수 있도록 active를 함께 준다. (COM-BRD-33)
public record AdminBoardCategoryResponse(
	UUID categoryUuid,
	String name,
	int sortOrder,
	boolean active
) {

	public static AdminBoardCategoryResponse from(BoardCategory category) {
		return new AdminBoardCategoryResponse(
			category.getUuid(), category.getName(), category.getSortOrder(), category.isActive());
	}
}
