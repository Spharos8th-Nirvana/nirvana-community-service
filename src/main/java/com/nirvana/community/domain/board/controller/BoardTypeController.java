package com.nirvana.community.domain.board.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nirvana.community.domain.board.dto.BoardTypeResponse;
import com.nirvana.community.domain.board.service.BoardTypeService;
import com.nirvana.community.global.auth.CurrentRequester;
import com.nirvana.community.global.auth.Requester;
import com.nirvana.community.global.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

// 누구나 볼 수 있는 게시판 종류 조회. 로그인은 선택이고, 비회원이면 requester가 null이다.
@Tag(name = "게시판 종류", description = "누구나 볼 수 있는 게시판 종류 조회")
@RestController
@RequiredArgsConstructor
public class BoardTypeController {

	private final BoardTypeService boardTypeService;

	@Operation(summary = "게시판 종류 목록 조회 (COM-BRD-10)",
		description = "운영 중(active)인 게시판 종류와 기능 스위치(댓글, 좋아요, 이미지, 예약 공개, 카테고리, 상단 고정, 고정 순서 정렬, 상단 고정 최대 개수)를 준다. "
			+ "요청자 역할 기준 열람·작성 가능 여부(canRead, canWrite)도 함께 준다. 권한 행이 없으면 둘 다 false다. "
			+ "로그인은 선택이다. 인증 헤더가 둘 다 없으면 비회원(GUEST) 기준으로 계산한다.")
	@GetMapping("/api/v1/boards")
	public ApiResponse<List<BoardTypeResponse>> getBoardTypes(
		@Parameter(hidden = true) @CurrentRequester(required = false) Requester requester) {
		return ApiResponse.success(boardTypeService.getActiveBoardTypes(requester));
	}
}
