package com.nirvana.community.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

	private final HttpStatus status;
	private final String message;

	// 응답의 error.code는 상수 이름을 그대로 써서 UPPER_SNAKE_CASE를 보장한다.
	public String getCode() {
		return name();
	}
}
