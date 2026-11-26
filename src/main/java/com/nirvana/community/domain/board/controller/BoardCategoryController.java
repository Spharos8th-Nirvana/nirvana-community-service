package com.nirvana.community.domain.board.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.nirvana.community.domain.board.dto.BoardCategoryResponse;
import com.nirvana.community.domain.board.service.BoardCategoryService;
import com.nirvana.community.global.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

// 누구나 볼 수 있는 카테고리 목록. 로그인이 필요 없다. (COM-BRD-20)
@Tag(name = "게시판 카테고리", description = "게시판 카테고리 조회")
@RestController
@RequiredArgsConstructor
public class BoardCategoryController {

	private final BoardCategoryService boardCategoryService;

	@Operation(
		summary = "카테고리 목록 조회",
		description = "게시판의 사용 중인 카테고리를 sortOrder 오름차순으로 조회한다. 보관된 카테고리는 빠진다. "
			+ "카테고리를 쓰지 않는 게시판이면 빈 목록이다. 로그인 없이 호출할 수 있다. "
			+ "게시판이 없거나 운영 중이 아니면 404. (COM-BRD-20)")
	@GetMapping("/api/v1/boards/{boardCode}/categories")
	public ApiResponse<List<BoardCategoryResponse>> getCategories(@PathVariable String boardCode) {
		return ApiResponse.success(boardCategoryService.getActiveCategories(boardCode));
	}
}
