package com.nirvana.community.global.auth;

// 요청자 역할. Gateway의 X-Account-Type 값과 같고(AUTH-33), 권한·작성자·신고자 구분과 응답에서도 이 값만 쓴다. (COM-NFR-10)
public enum AccountType {

	USER,
	OPERATOR,
	ADMIN
}
