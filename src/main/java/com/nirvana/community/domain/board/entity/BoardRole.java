package com.nirvana.community.domain.board.entity;

import org.jspecify.annotations.Nullable;

import com.nirvana.community.global.auth.Requester;

// 게시판 권한(boardPermissions.role)을 정할 때 쓰는 역할. 작성자 구분(AccountType)과 달리 비회원(GUEST)이 있다. (요구사항 2장)
public enum BoardRole {

	GUEST,
	USER,
	OPERATOR,
	ADMIN;

	// 로그인하지 않은 요청은 Requester가 없으므로 GUEST로 본다.
	public static BoardRole from(@Nullable Requester requester) {
		if (requester == null) {
			return GUEST;
		}
		return switch (requester.accountType()) {
			case USER -> USER;
			case OPERATOR -> OPERATOR;
			case ADMIN -> ADMIN;
		};
	}

	// 글·댓글에는 작성자 UUID가 꼭 있어야 해서 비회원에게는 작성 권한을 줄 수 없다.
	public boolean canHaveWritePermission() {
		return this != GUEST;
	}
}
