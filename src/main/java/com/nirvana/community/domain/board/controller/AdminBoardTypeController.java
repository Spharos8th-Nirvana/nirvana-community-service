package com.nirvana.community.domain.board.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nirvana.community.domain.board.dto.BoardTypeResponse;
import com.nirvana.community.domain.board.dto.CreateBoardTypeRequest;
import com.nirvana.community.domain.board.dto.UpdateBoardTypeRequest;
import com.nirvana.community.domain.board.service.BoardTypeService;
import com.nirvana.community.global.auth.AdminOnly;
import com.nirvana.community.global.auth.CurrentRequester;
import com.nirvana.community.global.auth.Requester;
import com.nirvana.community.global.response.ApiResponse;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// 플랫폼 관리자용 게시판 종류 관리. ADMIN인지는 @AdminOnly(인터셉터)가 Service 호출 전에 확인한다.
@AdminOnly
@RestController
@RequestMapping("/api/v1/admin/boards")
@RequiredArgsConstructor
public class AdminBoardTypeController {

	private final BoardTypeService boardTypeService;

	@PostMapping
	public ResponseEntity<ApiResponse<BoardTypeResponse>> create(
		@Parameter(hidden = true) @CurrentRequester Requester requester,
		@Valid @RequestBody CreateBoardTypeRequest request) {
		BoardTypeResponse response = boardTypeService.create(requester, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
	}

	@PatchMapping("/{boardCode}")
	public ApiResponse<BoardTypeResponse> update(
		@PathVariable String boardCode,
		@Parameter(hidden = true) @CurrentRequester Requester requester,
		@Valid @RequestBody UpdateBoardTypeRequest request) {
		return ApiResponse.success(boardTypeService.update(requester, boardCode, request));
	}

	@PostMapping("/{boardCode}/deactivate")
	public ResponseEntity<Void> deactivate(
		@PathVariable String boardCode,
		@Parameter(hidden = true) @CurrentRequester Requester requester) {
		boardTypeService.deactivate(requester, boardCode);
		return ResponseEntity.noContent().build();
	}
}
