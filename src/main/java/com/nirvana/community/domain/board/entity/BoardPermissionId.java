package com.nirvana.community.domain.board.entity;

import java.io.Serializable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

// boardPermissions의 복합 키(boardTypeId, role). BoardPermission의 @IdClass로 쓴다.
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class BoardPermissionId implements Serializable {

	private Long boardTypeId;
	private BoardRole role;
}
