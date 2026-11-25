package com.nirvana.community.domain.board.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.nirvana.community.domain.board.dto.BoardCategoryResponse;
import com.nirvana.community.domain.board.service.BoardCategoryService;
import com.nirvana.community.global.response.ApiResponse;

import lombok.RequiredArgsConstructor;

// 누구나 볼 수 있는 카테고리 목록. 로그인이 필요 없다. (COM-BRD-20)
@RestController
@RequiredArgsConstructor
public class BoardCategoryController {

	private final BoardCategoryService boardCategoryService;

	@GetMapping("/api/v1/boards/{boardCode}/categories")
	public ApiResponse<List<BoardCategoryResponse>> getCategories(@PathVariable String boardCode) {
		return ApiResponse.success(boardCategoryService.getActiveCategories(boardCode));
	}
}
