package com.nirvana.community.domain.board.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nirvana.community.domain.board.dto.BoardPermissionsResponse;
import com.nirvana.community.domain.board.dto.UpdateBoardPermissionsRequest;
import com.nirvana.community.domain.board.service.BoardPermissionService;
import com.nirvana.community.global.auth.AdminOnly;
import com.nirvana.community.global.auth.CurrentRequester;
import com.nirvana.community.global.auth.Requester;
import com.nirvana.community.global.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// 플랫폼 관리자용 게시판 권한 관리. ADMIN인지는 @AdminOnly(인터셉터)가 Service 호출 전에 확인한다. (COM-BRD-13)
@Tag(name = "게시판 권한 관리 (관리자)", description = "게시판 종류별 역할의 열람·작성 권한 조회·수정. ADMIN만 호출 가능")
@AdminOnly
@RestController
@RequestMapping("/api/v1/admin/boards/{boardCode}/permissions")
@RequiredArgsConstructor
public class AdminBoardPermissionController {

	private final BoardPermissionService boardPermissionService;

	@Operation(summary = "게시판 권한 조회 (COM-BRD-13)",
		description = "역할 4개(GUEST, USER, OPERATOR, ADMIN)의 열람·작성 권한을 모두 준다. 권한 행이 없는 역할은 둘 다 false다. ADMIN만 호출 가능, 아니면 403. 인증 헤더가 없거나 틀리면 401.")
	@GetMapping
	public ApiResponse<BoardPermissionsResponse> getPermissions(
		@PathVariable String boardCode) {
		return ApiResponse.success(boardPermissionService.getPermissions(boardCode));
	}

	@Operation(summary = "게시판 권한 수정 (COM-BRD-13)",
		description = "보낸 역할의 권한만 저장한다. 행이 없으면 만들고 있으면 바꾼다. 같은 역할을 두 번 보낼 수 없고, "
			+ "비회원(GUEST)에게는 작성 권한을 줄 수 없다. 저장 후 역할 4개의 권한을 모두 준다. ADMIN만 호출 가능, 아니면 403. 인증 헤더가 없거나 틀리면 401.")
	@PutMapping
	public ApiResponse<BoardPermissionsResponse> updatePermissions(
		@PathVariable String boardCode,
		@Parameter(hidden = true) @CurrentRequester Requester requester,
		@Valid @RequestBody UpdateBoardPermissionsRequest request) {
		return ApiResponse.success(boardPermissionService.updatePermissions(requester, boardCode, request));
	}
}
