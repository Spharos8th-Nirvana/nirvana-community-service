package com.nirvana.community.domain.post.service;

import org.springframework.stereotype.Component;

import com.nirvana.community.global.auth.Requester;

// 임시 구현이다. Member·Temple 연동이 아직 없어 역할별 고정 이름을 쓴다.
// 연동이 생기면 닉네임(USER)·소속 사찰명(OPERATOR)을 받아 오는 구현으로 바꾸고 이 클래스는 지운다. (COM-INT-01)
// ADMIN의 '플랫폼 관리자'는 요구사항에 정해진 값이라 연동 후에도 같다.
@Component
public class TemporaryAuthorNameProvider implements AuthorNameProvider {

	private static final String TEMPORARY_USER_NAME = "회원";
	private static final String TEMPORARY_OPERATOR_NAME = "사찰 운영자";
	private static final String ADMIN_NAME = "플랫폼 관리자";

	@Override
	public String getAuthorName(Requester requester) {
		return switch (requester.accountType()) {
			case USER -> TEMPORARY_USER_NAME;
			case OPERATOR -> TEMPORARY_OPERATOR_NAME;
			case ADMIN -> ADMIN_NAME;
		};
	}
}
