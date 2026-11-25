package com.nirvana.community.domain.board.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nirvana.community.domain.board.dto.AdminBoardCategoryResponse;
import com.nirvana.community.domain.board.dto.CreateBoardCategoryRequest;
import com.nirvana.community.domain.board.dto.UpdateBoardCategoryRequest;
import com.nirvana.community.domain.board.service.BoardCategoryService;
import com.nirvana.community.global.auth.CurrentRequester;
import com.nirvana.community.global.auth.Requester;
import com.nirvana.community.global.response.ApiResponse;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// 플랫폼 관리자용 카테고리 관리. ADMIN인지는 Service가 확인한다.
// 등록·목록은 게시판 아래에 두고, 카테고리 하나를 다루는 API는 categoryUuid로 가리킨다.
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminBoardCategoryController {

	private final BoardCategoryService boardCategoryService;

	@GetMapping("/boards/{boardCode}/categories")
	public ApiResponse<List<AdminBoardCategoryResponse>> getCategories(
		@PathVariable String boardCode,
		@Parameter(hidden = true) @CurrentRequester Requester requester) {
		return ApiResponse.success(boardCategoryService.getAllCategories(requester, boardCode));
	}

	@PostMapping("/boards/{boardCode}/categories")
	public ResponseEntity<ApiResponse<AdminBoardCategoryResponse>> create(
		@PathVariable String boardCode,
		@Parameter(hidden = true) @CurrentRequester Requester requester,
		@Valid @RequestBody CreateBoardCategoryRequest request) {
		AdminBoardCategoryResponse response = boardCategoryService.create(requester, boardCode, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
	}

	@PatchMapping("/categories/{categoryUuid}")
	public ApiResponse<AdminBoardCategoryResponse> update(
		@PathVariable UUID categoryUuid,
		@Parameter(hidden = true) @CurrentRequester Requester requester,
		@Valid @RequestBody UpdateBoardCategoryRequest request) {
		return ApiResponse.success(boardCategoryService.update(requester, categoryUuid, request));
	}

	@PostMapping("/categories/{categoryUuid}/archive")
	public ResponseEntity<Void> archive(
		@PathVariable UUID categoryUuid,
		@Parameter(hidden = true) @CurrentRequester Requester requester) {
		boardCategoryService.archive(requester, categoryUuid);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/categories/{categoryUuid}/restore")
	public ResponseEntity<Void> restore(
		@PathVariable UUID categoryUuid,
		@Parameter(hidden = true) @CurrentRequester Requester requester) {
		boardCategoryService.restore(requester, categoryUuid);
		return ResponseEntity.noContent().build();
	}
}
