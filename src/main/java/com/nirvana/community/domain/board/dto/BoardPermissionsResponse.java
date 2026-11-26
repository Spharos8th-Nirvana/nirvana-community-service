package com.nirvana.community.domain.board.dto;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.nirvana.community.domain.board.entity.BoardPermission;
import com.nirvana.community.domain.board.entity.BoardRole;

// 한 게시판의 역할별 열람·작성 권한. 역할 4개를 항상 모두 준다. (COM-BRD-13)
public record BoardPermissionsResponse(
	String boardCode,
	List<RolePermission> permissions
) {

	// 권한 행이 없는 역할은 열람·작성 모두 거절이므로 둘 다 false로 준다. (COM-BRD-01)
	public static BoardPermissionsResponse of(String boardCode, Map<BoardRole, BoardPermission> permissionsByRole) {
		List<RolePermission> permissions = Arrays.stream(BoardRole.values())
			.map(role -> RolePermission.of(role, Optional.ofNullable(permissionsByRole.get(role))))
			.toList();
		return new BoardPermissionsResponse(boardCode, permissions);
	}

	public record RolePermission(
		BoardRole role,
		boolean canRead,
		boolean canWrite
	) {

		private static RolePermission of(BoardRole role, Optional<BoardPermission> permission) {
			return new RolePermission(
				role,
				permission.map(BoardPermission::isCanRead).orElse(false),
				permission.map(BoardPermission::isCanWrite).orElse(false));
		}
	}
}
