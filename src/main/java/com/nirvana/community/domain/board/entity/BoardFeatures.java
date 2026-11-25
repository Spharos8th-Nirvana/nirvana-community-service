package com.nirvana.community.domain.board.entity;

// 게시판 종류의 기능 스위치 묶음. 등록과 수정에서 같은 값을 받으므로 한 덩어리로 넘긴다.
public record BoardFeatures (
	boolean canSortOrder,
	boolean allowComment,
	boolean allowLike,
	boolean allowImage,
	boolean allowScheduledPublish,
	boolean useCategory,
	boolean allowPin,
	int maxPinCount
) {

}
