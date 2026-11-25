package com.nirvana.community.domain.board.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nirvana.community.domain.board.dto.BoardTypeResponse;
import com.nirvana.community.domain.board.dto.CreateBoardTypeRequest;
import com.nirvana.community.domain.board.dto.UpdateBoardTypeRequest;
import com.nirvana.community.domain.board.entity.BoardType;
import com.nirvana.community.domain.board.repository.BoardTypeRepository;
import com.nirvana.community.global.auth.AccountType;
import com.nirvana.community.global.auth.Requester;
import com.nirvana.community.global.exception.BusinessException;
import com.nirvana.community.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardTypeService {

	private final BoardTypeRepository boardTypeRepository;

	// 운영 중인 게시판 종류만 준다. (COM-BRD-10)
	@Transactional(readOnly = true)
	public List<BoardTypeResponse> getActiveBoardTypes() {
		return boardTypeRepository.findAllByActiveTrueOrderByIdAsc().stream()
			.map(BoardTypeResponse::from)
			.toList();
	}

	// code는 중복될 수 없다. (COM-BRD-11)
	@Transactional
	public BoardTypeResponse create(Requester requester, CreateBoardTypeRequest request) {
		validateAdmin(requester);
		if (boardTypeRepository.existsByCode(request.code())) {
			throw new BusinessException(ErrorCode.DUPLICATE_BOARD_CODE);
		}
		BoardType boardType = BoardType.create(
			request.code(), request.name(), request.toFeatures(), requester.accountUuid());
		boardTypeRepository.save(boardType);
		return BoardTypeResponse.from(boardType);
	}

	@Transactional
	public BoardTypeResponse update(Requester requester, String boardCode, UpdateBoardTypeRequest request) {
		validateAdmin(requester);
		BoardType boardType = findByCode(boardCode);
		boardType.update(request.name(), request.toFeatures(), requester.accountUuid());
		return BoardTypeResponse.from(boardType);
	}

	// 삭제하지 않고 끈다. (COM-BRD-12)
	@Transactional
	public void deactivate(Requester requester, String boardCode) {
		validateAdmin(requester);
		findByCode(boardCode).deactivate(requester.accountUuid());
	}

	private void validateAdmin(Requester requester) {
		if (requester.accountType() != AccountType.ADMIN) {
			throw new BusinessException(ErrorCode.FORBIDDEN);
		}
	}

	private BoardType findByCode(String boardCode) {
		return boardTypeRepository.findByCode(boardCode)
			.orElseThrow(() -> new BusinessException(ErrorCode.BOARD_NOT_FOUND));
	}
}
