package com.nirvana.community.domain.board.dto;

import java.util.List;
import java.util.Objects;

import com.nirvana.community.domain.board.entity.BoardRole;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

// 보낸 역할만 저장한다. 행이 없으면 만들고 있으면 바꾼다. (COM-BRD-13)
public record UpdateBoardPermissionsRequest(

	@NotEmpty(message = "바꿀 권한을 하나 이상 입력해주세요.")
	List<@NotNull(message = "권한을 입력해주세요.") @Valid Permission> permissions
) {

	@AssertTrue(message = "같은 역할을 두 번 보낼 수 없습니다.")
	public boolean isRolesUnique() {
		if (permissions == null) {
			return true;
		}
		List<BoardRole> roles = permissions.stream()
			.filter(Objects::nonNull)
			.map(Permission::role)
			.filter(Objects::nonNull)
			.toList();
		return roles.size() == roles.stream().distinct().count();
	}

	public record Permission(

		@NotNull(message = "역할을 입력해주세요.")
		BoardRole role,

		@NotNull(message = "열람 가능 여부를 입력해주세요.")
		Boolean canRead,

		@NotNull(message = "작성 가능 여부를 입력해주세요.")
		Boolean canWrite
	) {

		@AssertTrue(message = "비회원(GUEST)에게는 작성 권한을 줄 수 없습니다.")
		public boolean isWritePermissionAllowedForRole() {
			if (role == null || canWrite == null) {
				return true;
			}
			return !canWrite || role.canHaveWritePermission();
		}
	}
}
