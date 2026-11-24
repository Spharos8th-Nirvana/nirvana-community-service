package com.nirvana.community.global.response;

// traceId는 구할 수 없으면 null로라도 항상 내보낸다. (COM-NFR-09)
public record ErrorResponse(
	String code,
	String message,
	String traceId
) {
}
