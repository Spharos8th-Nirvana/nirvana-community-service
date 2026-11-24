package com.nirvana.community.global.response;

public record ErrorResponse(
	String code,
	String message
) {
}
