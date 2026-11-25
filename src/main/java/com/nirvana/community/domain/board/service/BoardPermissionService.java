package com.nirvana.community.domain.board.service;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nirvana.community.domain.board.dto.BoardPermissionsResponse;
import com.nirvana.community.domain.board.dto.UpdateBoardPermissionsRequest;
import com.nirvana.community.domain.board.entity.BoardPermission;
import com.nirvana.community.domain.board.entity.BoardPermissionId;
import com.nirvana.community.domain.board.entity.BoardRole;
import com.nirvana.community.domain.board.entity.BoardType;
import com.nirvana.community.domain.board.repository.BoardPermissionRepository;
import com.nirvana.community.domain.board.repository.BoardTypeRepository;
import com.nirvana.community.global.auth.AccountType;
import com.nirvana.community.global.auth.Requester;
import com.nirvana.community.global.exception.BusinessException;
import com.nirvana.community.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

// 플랫폼 관리자의 게시판 권한 조회·수정. (COM-BRD-13)
@Service
@RequiredArgsConstructor
public class BoardPermissionService {

	private final BoardTypeRepository boardTypeRepository;
	private final BoardPermissionRepository boardPermissionRepository;

	@Transactional(readOnly = true)
	public BoardPermissionsResponse getPermissions(Requester requester, String boardCode) {
		validateAdmin(requester);
		return toResponse(getBoardType(boardCode));
	}

	// 수정한 관리자를 updatedBy에 남긴다. (COM-NFR-06)
	@Transactional
	public BoardPermissionsResponse updatePermissions(Requester requester, String boardCode,
		UpdateBoardPermissionsRequest request) {
		validateAdmin(requester);
		BoardType boardType = getBoardType(boardCode);
		request.permissions().forEach(permission ->
			savePermission(boardType.getId(), permission, requester.accountUuid()));
		return toResponse(boardType);
	}

	private void savePermission(Long boardTypeId, UpdateBoardPermissionsRequest.Permission request, UUID adminUuid) {
		boardPermissionRepository.findById(new BoardPermissionId(boardTypeId, request.role()))
			.ifPresentOrElse(
				permission -> permission.change(request.canRead(), request.canWrite(), adminUuid),
				() -> boardPermissionRepository.save(BoardPermission.create(
					boardTypeId, request.role(), request.canRead(), request.canWrite(), adminUuid)));
	}

	private BoardPermissionsResponse toResponse(BoardType boardType) {
		Map<BoardRole, BoardPermission> permissionsByRole =
			boardPermissionRepository.findAllByBoardTypeId(boardType.getId()).stream()
				.collect(Collectors.toMap(BoardPermission::getRole, Function.identity()));
		return BoardPermissionsResponse.of(boardType.getCode(), permissionsByRole);
	}

	private void validateAdmin(Requester requester) {
		if (requester.accountType() != AccountType.ADMIN) {
			throw new BusinessException(ErrorCode.FORBIDDEN);
		}
	}

	private BoardType getBoardType(String boardCode) {
		return boardTypeRepository.findByCode(boardCode)
			.orElseThrow(() -> new BusinessException(ErrorCode.BOARD_NOT_FOUND));
	}
}
