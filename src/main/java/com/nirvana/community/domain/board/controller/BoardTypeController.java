package com.nirvana.community.domain.board.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nirvana.community.domain.board.dto.BoardTypeResponse;
import com.nirvana.community.domain.board.service.BoardTypeService;
import com.nirvana.community.global.auth.CurrentRequester;
import com.nirvana.community.global.auth.Requester;
import com.nirvana.community.global.response.ApiResponse;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;

// 누구나 볼 수 있는 게시판 종류 조회. 로그인은 선택이고, 비회원이면 requester가 null이다.
@RestController
@RequiredArgsConstructor
public class BoardTypeController {

	private final BoardTypeService boardTypeService;

	@GetMapping("/api/v1/boards")
	public ApiResponse<List<BoardTypeResponse>> getBoardTypes(
		@Parameter(hidden = true) @CurrentRequester(required = false) Requester requester) {
		return ApiResponse.success(boardTypeService.getActiveBoardTypes(requester));
	}
}
