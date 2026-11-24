package com.nirvana.community.global.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

// traceId는 구할 수 없으면 null로라도 항상 내보내고, details는 입력 오류일 때만 내보낸다. (COM-NFR-09)
public record ErrorResponse(
	String code,
	String message,
	@JsonInclude(JsonInclude.Include.NON_NULL)
	List<ErrorDetail> details,
	String traceId
) {

	public ErrorResponse(String code, String message, String traceId) {
		this(code, message, null, traceId);
	}
}
