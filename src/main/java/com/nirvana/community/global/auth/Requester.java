package com.nirvana.community.global.auth;

import java.util.UUID;

// Gateway가 넘겨 준 인증 헤더로 만든 요청자 정보. 요청 본문의 작성자 값 대신 이것만 믿는다. (COM-BRD-02)
public record Requester(
	UUID accountUuid,
	AccountType accountType
) {
}
