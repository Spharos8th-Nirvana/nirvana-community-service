package com.nirvana.community.domain.board.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nirvana.community.domain.board.dto.BoardTypeResponse;
import com.nirvana.community.domain.board.dto.CreateBoardTypeRequest;
import com.nirvana.community.domain.board.dto.UpdateBoardTypeRequest;
import com.nirvana.community.domain.board.entity.BoardPermission;
import com.nirvana.community.domain.board.entity.BoardPermissionId;
import com.nirvana.community.domain.board.entity.BoardRole;
import com.nirvana.community.domain.board.entity.BoardType;
import com.nirvana.community.domain.board.repository.BoardPermissionRepository;
import com.nirvana.community.domain.board.repository.BoardTypeRepository;
import com.nirvana.community.global.auth.Requester;
import com.nirvana.community.global.exception.BusinessException;
import com.nirvana.community.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardTypeService {

	private final BoardTypeRepository boardTypeRepository;
	private final BoardPermissionRepository boardPermissionRepository;

	// 운영 중인 게시판 종류만, 요청자 역할 기준 열람·작성 가능 여부와 함께 준다. 비회원은 GUEST로 본다. (COM-BRD-10)
	@Transactional(readOnly = true)
	public List<BoardTypeResponse> getActiveBoardTypes(@Nullable Requester requester) {
		Map<Long, BoardPermission> permissionsByBoardTypeId = findPermissionsByBoardTypeId(BoardRole.from(requester));
		return boardTypeRepository.findAllByActiveTrueOrderByIdAsc().stream()
			.map(boardType -> BoardTypeResponse.from(
				boardType, Optional.ofNullable(permissionsByBoardTypeId.get(boardType.getId()))))
			.toList();
	}

	// code는 중복될 수 없다. (COM-BRD-11)
	@Transactional
	public BoardTypeResponse create(Requester requester, CreateBoardTypeRequest request) {
		if (boardTypeRepository.existsByCode(request.code())) {
			throw new BusinessException(ErrorCode.DUPLICATE_BOARD_CODE);
		}
		BoardType boardType = BoardType.create(
			request.code(), request.name(), request.toFeatures(), requester.accountUuid());
		boardTypeRepository.save(boardType);
		// 새 게시판에는 아직 권한 행이 없다.
		return BoardTypeResponse.from(boardType, Optional.empty());
	}

	@Transactional
	public BoardTypeResponse update(Requester requester, String boardCode, UpdateBoardTypeRequest request) {
		BoardType boardType = findByCode(boardCode);
		boardType.update(request.name(), request.toFeatures(), requester.accountUuid());
		return BoardTypeResponse.from(boardType, findPermission(boardType, requester));
	}

	// 삭제하지 않고 끈다. (COM-BRD-12)
	@Transactional
	public void deactivate(Requester requester, String boardCode) {
		findByCode(boardCode).deactivate(requester.accountUuid());
	}

	// 게시판마다 따로 조회하지 않도록 그 역할의 권한을 한 번에 읽는다.
	private Map<Long, BoardPermission> findPermissionsByBoardTypeId(BoardRole role) {
		return boardPermissionRepository.findAllByRole(role).stream()
			.collect(Collectors.toMap(BoardPermission::getBoardTypeId, Function.identity()));
	}

	private Optional<BoardPermission> findPermission(BoardType boardType, Requester requester) {
		return boardPermissionRepository.findById(new BoardPermissionId(boardType.getId(), BoardRole.from(requester)));
	}

	private BoardType findByCode(String boardCode) {
		return boardTypeRepository.findByCode(boardCode)
			.orElseThrow(() -> new BusinessException(ErrorCode.BOARD_NOT_FOUND));
	}
}
