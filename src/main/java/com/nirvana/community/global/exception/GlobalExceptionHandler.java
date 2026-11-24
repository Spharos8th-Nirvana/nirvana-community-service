package com.nirvana.community.global.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.nirvana.community.global.response.ApiResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
		ErrorCode errorCode = exception.getErrorCode();
		log.warn("Business exception occurred: code={}", errorCode.getCode());
		return createErrorResponse(errorCode);
	}

	// 내부 정보(메시지, SQL, 스택 트레이스)는 로그에만 남기고 응답에는 일반화된 메시지만 준다.
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception exception) {
		log.error("Unexpected exception occurred", exception);
		return createErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR);
	}

	private ResponseEntity<ApiResponse<Void>> createErrorResponse(ErrorCode errorCode) {
		return ResponseEntity
			.status(errorCode.getStatus())
			.body(ApiResponse.failure(errorCode.getCode(), errorCode.getMessage()));
	}
}
