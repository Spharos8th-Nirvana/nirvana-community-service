package com.nirvana.community.domain.board.dto;

import com.nirvana.community.domain.board.entity.BoardType;

// 화면이 글쓰기 버튼, 댓글창, 좋아요 버튼을 그릴지 정하는 데 쓰는 값. (COM-BRD-10)
// COM-BRD-10 중 기능 스위치만 담는다. 요청자 역할 기준 열람·작성 가능 여부(canRead, canWrite)는
// boardPermissions가 필요해서 게시판 권한 작업(#19)에서 추가한다.
public record BoardTypeResponse(
	String code,
	String name,
	boolean canSortOrder,
	boolean allowComment,
	boolean allowLike,
	boolean allowImage,
	boolean allowScheduledPublish,
	boolean useCategory,
	boolean allowPin,
	int maxPinCount
) {

	public static BoardTypeResponse from(BoardType boardType) {
		return new BoardTypeResponse(
			boardType.getCode(),
			boardType.getName(),
			boardType.isCanSortOrder(),
			boardType.isAllowComment(),
			boardType.isAllowLike(),
			boardType.isAllowImage(),
			boardType.isAllowScheduledPublish(),
			boardType.isUseCategory(),
			boardType.isAllowPin(),
			boardType.getMaxPinCount());
	}
}
