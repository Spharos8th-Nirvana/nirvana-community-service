package com.nirvana.community.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;

// 화면이 항상 같은 세 필드로 분기할 수 있도록 null 필드도 내보낸다. (COM-NFR-07)
@JsonInclude(JsonInclude.Include.ALWAYS)
public record ApiResponse<T>(
	boolean success,
	T data,
	ErrorResponse error
) {

	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(true, data, null);
	}

	public static ApiResponse<Void> failure(String code, String message) {
		return new ApiResponse<>(false, null, new ErrorResponse(code, message));
	}
}
