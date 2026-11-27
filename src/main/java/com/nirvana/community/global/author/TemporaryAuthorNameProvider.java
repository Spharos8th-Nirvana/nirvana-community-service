package com.nirvana.community.global.author;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.nirvana.community.global.auth.Requester;

// 임시 구현이다. Member·Temple 연동이 아직 없어 역할별 고정 이름을 쓴다.
// 연동이 생기면 닉네임(USER)·소속 사찰명(OPERATOR)을 받아 오는 구현으로 바꾸고 이 클래스는 지운다. (COM-INT-01)
// ADMIN의 '플랫폼 관리자'는 요구사항에 정해진 값이라 연동 후에도 같다.
// local 프로필에서만 빈으로 등록한다. 다른 프로필에서는 AuthorNameProvider 빈이 없어 앱이 뜨지 않는데, 의도한 것이다.
// 임시 이름이 개발·운영 DB에 몰래 저장되는 것보다 시작할 때 바로 실패하는 편이 낫다.
@Profile("local")
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
