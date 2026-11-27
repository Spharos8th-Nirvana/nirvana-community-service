package com.nirvana.community.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "입력값을 확인해주세요."),
	INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
	FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
	NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 주소를 찾을 수 없습니다."),
	METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "허용하지 않는 메서드입니다."),
	BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "게시판을 찾을 수 없습니다."),
	DUPLICATE_BOARD_CODE(HttpStatus.CONFLICT, "이미 있는 게시판 코드입니다."),
	CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다."),
	DUPLICATE_CATEGORY_NAME(HttpStatus.CONFLICT, "이 게시판에 이미 있는 카테고리 이름입니다."),
	REVIEW_BOARD_WRITE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "리뷰 게시판에는 일반 글을 쓸 수 없습니다."),
	CATEGORY_REQUIRED(HttpStatus.BAD_REQUEST, "카테고리를 선택해주세요."),
	CATEGORY_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "카테고리를 쓰지 않는 게시판입니다."),
	INVALID_CATEGORY(HttpStatus.BAD_REQUEST, "이 게시판에서 고를 수 있는 카테고리가 아닙니다."),
	POST_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."),
	POST_ALREADY_DELETED(HttpStatus.CONFLICT, "삭제된 게시글은 바꿀 수 없습니다."),
	POST_NOT_DRAFT(HttpStatus.CONFLICT, "임시저장 글만 공개할 수 있습니다."),
	POST_CONTENT_EMPTY(HttpStatus.BAD_REQUEST, "본문에 내용을 입력해주세요. 허용하지 않는 태그와 이미지는 저장되지 않습니다."),
	INVALID_PARENT_COMMENT(HttpStatus.BAD_REQUEST, "답글을 달 수 있는 댓글이 아닙니다."),
	COMMENT_DEPTH_EXCEEDED(HttpStatus.BAD_REQUEST, "이 댓글에는 더 이상 답글을 달 수 없습니다."),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

	private final HttpStatus status;
	private final String message;

	// 응답의 error.code는 상수 이름을 그대로 써서 UPPER_SNAKE_CASE를 보장한다.
	public String getCode() {
		return name();
	}
}
