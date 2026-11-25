package com.nirvana.community.domain.board.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nirvana.community.domain.board.dto.BoardTypeResponse;
import com.nirvana.community.domain.board.service.BoardTypeService;
import com.nirvana.community.global.response.ApiResponse;

import lombok.RequiredArgsConstructor;

// 누구나 볼 수 있는 게시판 종류 조회. 로그인이 필요 없다.
@RestController
@RequiredArgsConstructor
public class BoardTypeController {

	private final BoardTypeService boardTypeService;

	@GetMapping("/api/v1/boards")
	public ApiResponse<List<BoardTypeResponse>> getBoardTypes() {
		return ApiResponse.success(boardTypeService.getActiveBoardTypes());
	}
}
