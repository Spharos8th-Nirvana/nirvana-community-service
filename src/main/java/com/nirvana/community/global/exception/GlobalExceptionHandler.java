package com.nirvana.community.global.exception;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.nirvana.community.global.response.ApiResponse;
import com.nirvana.community.global.response.ErrorResponse;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

	// 트레이싱이 꺼진 환경에서도 오류 응답은 나가야 하므로 Tracer 빈이 없어도 되게 받는다.
	private final ObjectProvider<Tracer> tracerProvider;

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
		ErrorResponse error = new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), findTraceId());
		return ResponseEntity
			.status(errorCode.getStatus())
			.body(ApiResponse.failure(error));
	}

	private String findTraceId() {
		Tracer tracer = tracerProvider.getIfAvailable();
		if (tracer == null) {
			return null;
		}
		Span span = tracer.currentSpan();
		if (span == null || !StringUtils.hasText(span.context().traceId())) {
			return null;
		}
		return span.context().traceId();
	}
}
