package com.nirvana.community.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
	NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 주소를 찾을 수 없습니다."),
	METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "허용하지 않는 메서드입니다."),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

	private final HttpStatus status;
	private final String message;

	// 응답의 error.code는 상수 이름을 그대로 써서 UPPER_SNAKE_CASE를 보장한다.
	public String getCode() {
		return name();
	}
}
