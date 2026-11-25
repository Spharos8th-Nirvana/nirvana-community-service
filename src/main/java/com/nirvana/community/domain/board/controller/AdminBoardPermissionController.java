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
import com.nirvana.community.global.auth.CurrentRequester;
import com.nirvana.community.global.auth.Requester;
import com.nirvana.community.global.response.ApiResponse;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// 플랫폼 관리자용 게시판 권한 관리. ADMIN인지는 Service가 확인한다. (COM-BRD-13)
@RestController
@RequestMapping("/api/v1/admin/boards/{boardCode}/permissions")
@RequiredArgsConstructor
public class AdminBoardPermissionController {

	private final BoardPermissionService boardPermissionService;

	@GetMapping
	public ApiResponse<BoardPermissionsResponse> getPermissions(
		@PathVariable String boardCode,
		@Parameter(hidden = true) @CurrentRequester Requester requester) {
		return ApiResponse.success(boardPermissionService.getPermissions(requester, boardCode));
	}

	@PutMapping
	public ApiResponse<BoardPermissionsResponse> updatePermissions(
		@PathVariable String boardCode,
		@Parameter(hidden = true) @CurrentRequester Requester requester,
		@Valid @RequestBody UpdateBoardPermissionsRequest request) {
		return ApiResponse.success(boardPermissionService.updatePermissions(requester, boardCode, request));
	}
}
