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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// 플랫폼 관리자용 게시판 종류 관리. ADMIN인지는 @AdminOnly(인터셉터)가 Service 호출 전에 확인한다.
@Tag(name = "게시판 종류 관리 (관리자)", description = "플랫폼 관리자의 게시판 종류 등록·수정·비활성화. ADMIN만 호출 가능")
@AdminOnly
@RestController
@RequestMapping("/api/v1/admin/boards")
@RequiredArgsConstructor
public class AdminBoardTypeController {

	private final BoardTypeService boardTypeService;

	@Operation(summary = "게시판 종류 등록 (COM-BRD-11)",
		description = "게시판 종류를 이름, 기능 스위치, 상단 고정 최대 개수(0~10, COM-BRD-14)와 함께 등록한다. code는 중복될 수 없고 등록 후 바꿀 수 없다. "
			+ "새 게시판에는 권한 행이 없어 canRead, canWrite가 false다. ADMIN만 호출 가능, 아니면 403. 인증 헤더가 없거나 틀리면 401.")
	@PostMapping
	public ResponseEntity<ApiResponse<BoardTypeResponse>> create(
		@Parameter(hidden = true) @CurrentRequester Requester requester,
		@Valid @RequestBody CreateBoardTypeRequest request) {
		BoardTypeResponse response = boardTypeService.create(requester, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
	}

	@Operation(summary = "게시판 종류 수정 (COM-BRD-11)",
		description = "이름, 기능 스위치, 상단 고정 최대 개수(0~10, COM-BRD-14)를 바꾼다. code는 바꿀 수 없다. ADMIN만 호출 가능, 아니면 403. 인증 헤더가 없거나 틀리면 401.")
	@PatchMapping("/{boardCode}")
	public ApiResponse<BoardTypeResponse> update(
		@PathVariable String boardCode,
		@Parameter(hidden = true) @CurrentRequester Requester requester,
		@Valid @RequestBody UpdateBoardTypeRequest request) {
		return ApiResponse.success(boardTypeService.update(requester, boardCode, request));
	}

	@Operation(summary = "게시판 종류 비활성화 (COM-BRD-12)",
		description = "게시판 종류를 삭제하지 않고 끈다. 비활성 게시판은 목록에서 빠진다. 성공하면 204. ADMIN만 호출 가능, 아니면 403. 인증 헤더가 없거나 틀리면 401.")
	@PostMapping("/{boardCode}/deactivate")
	public ResponseEntity<Void> deactivate(
		@PathVariable String boardCode,
		@Parameter(hidden = true) @CurrentRequester Requester requester) {
		boardTypeService.deactivate(requester, boardCode);
		return ResponseEntity.noContent().build();
	}
}
