package com.nirvana.community.domain.board.dto;

import java.util.Optional;

import com.nirvana.community.domain.board.entity.BoardPermission;
import com.nirvana.community.domain.board.entity.BoardType;

// 화면이 글쓰기 버튼, 댓글창, 좋아요 버튼을 그릴지 정하는 데 쓰는 값. (COM-BRD-10)
// 기능 스위치와 함께 요청자 역할 기준 열람·작성 가능 여부(canRead, canWrite)를 담는다.
// 권한 행이 없으면 열람·작성 모두 거절이므로 둘 다 false다. (COM-BRD-01)
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
	int maxPinCount,
	boolean canRead,
	boolean canWrite
) {

	public static BoardTypeResponse from(BoardType boardType, Optional<BoardPermission> permission) {
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
			boardType.getMaxPinCount(),
			permission.map(BoardPermission::isCanRead).orElse(false),
			permission.map(BoardPermission::isCanWrite).orElse(false));
	}
}
