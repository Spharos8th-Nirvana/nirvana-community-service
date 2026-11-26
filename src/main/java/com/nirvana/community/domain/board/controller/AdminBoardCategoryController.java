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
import com.nirvana.community.global.auth.AdminOnly;
import com.nirvana.community.global.auth.CurrentRequester;
import com.nirvana.community.global.auth.Requester;
import com.nirvana.community.global.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// 플랫폼 관리자용 카테고리 관리. ADMIN인지는 @AdminOnly(인터셉터)가 Service 호출 전에 확인한다.
// 등록·목록은 게시판 아래에 두고, 카테고리 하나를 다루는 API는 categoryUuid로 가리킨다.
@Tag(name = "게시판 카테고리 (관리자)", description = "플랫폼 관리자용 게시판 카테고리 관리")
@AdminOnly
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminBoardCategoryController {

	private final BoardCategoryService boardCategoryService;

	@Operation(
		summary = "관리용 카테고리 목록 조회",
		description = "보관된 카테고리까지 포함해 sortOrder 오름차순으로 조회한다. 응답의 active로 사용 여부를 구분한다. "
			+ "게시판이 없으면 404. ADMIN만 호출 가능, 아니면 403. (COM-BRD-33)")
	@GetMapping("/boards/{boardCode}/categories")
	public ApiResponse<List<AdminBoardCategoryResponse>> getCategories(
		@PathVariable String boardCode,
		@Parameter(hidden = true) @CurrentRequester Requester requester) {
		return ApiResponse.success(boardCategoryService.getAllCategories(requester, boardCode));
	}

	@Operation(
		summary = "카테고리 등록",
		description = "게시판에 카테고리를 등록한다. 성공하면 201. "
			+ "같은 게시판에 같은 이름이 있으면 409, 게시판이 없으면 404. ADMIN만 호출 가능, 아니면 403. (COM-BRD-21)")
	@PostMapping("/boards/{boardCode}/categories")
	public ResponseEntity<ApiResponse<AdminBoardCategoryResponse>> create(
		@PathVariable String boardCode,
		@Parameter(hidden = true) @CurrentRequester Requester requester,
		@Valid @RequestBody CreateBoardCategoryRequest request) {
		AdminBoardCategoryResponse response = boardCategoryService.create(requester, boardCode, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
	}

	@Operation(
		summary = "카테고리 수정",
		description = "카테고리의 이름과 순서만 바꾼다. 다른 게시판으로 옮길 수 없다. "
			+ "같은 게시판의 다른 카테고리와 이름이 겹치면 409, 카테고리가 없으면 404. ADMIN만 호출 가능, 아니면 403. (COM-BRD-21)")
	@PatchMapping("/categories/{categoryUuid}")
	public ApiResponse<AdminBoardCategoryResponse> update(
		@PathVariable UUID categoryUuid,
		@Parameter(hidden = true) @CurrentRequester Requester requester,
		@Valid @RequestBody UpdateBoardCategoryRequest request) {
		return ApiResponse.success(boardCategoryService.update(requester, categoryUuid, request));
	}

	@Operation(
		summary = "카테고리 보관",
		description = "카테고리를 삭제하지 않고 보관한다(active = false). 이미 보관된 것이어도 결과는 같다. "
			+ "성공하면 204, 카테고리가 없으면 404. ADMIN만 호출 가능, 아니면 403. (COM-BRD-22)")
	@PostMapping("/categories/{categoryUuid}/archive")
	public ResponseEntity<Void> archive(
		@PathVariable UUID categoryUuid,
		@Parameter(hidden = true) @CurrentRequester Requester requester) {
		boardCategoryService.archive(requester, categoryUuid);
		return ResponseEntity.noContent().build();
	}

	@Operation(
		summary = "카테고리 복원",
		description = "보관한 카테고리를 다시 사용 중으로 바꾼다(active = true). 이미 사용 중이어도 결과는 같다. "
			+ "성공하면 204, 카테고리가 없으면 404. ADMIN만 호출 가능, 아니면 403. (COM-BRD-33)")
	@PostMapping("/categories/{categoryUuid}/restore")
	public ResponseEntity<Void> restore(
		@PathVariable UUID categoryUuid,
		@Parameter(hidden = true) @CurrentRequester Requester requester) {
		boardCategoryService.restore(requester, categoryUuid);
		return ResponseEntity.noContent().build();
	}
}
