package com.nirvana.community.domain.board.service;

import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nirvana.community.domain.board.dto.AdminBoardCategoryResponse;
import com.nirvana.community.domain.board.dto.BoardCategoryResponse;
import com.nirvana.community.domain.board.dto.CreateBoardCategoryRequest;
import com.nirvana.community.domain.board.dto.UpdateBoardCategoryRequest;
import com.nirvana.community.domain.board.entity.BoardCategory;
import com.nirvana.community.domain.board.entity.BoardType;
import com.nirvana.community.domain.board.repository.BoardCategoryRepository;
import com.nirvana.community.domain.board.repository.BoardTypeRepository;
import com.nirvana.community.global.auth.Requester;
import com.nirvana.community.global.exception.BusinessException;
import com.nirvana.community.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardCategoryService {

	private final BoardCategoryRepository boardCategoryRepository;
	private final BoardTypeRepository boardTypeRepository;

	// 운영 중인 게시판의 사용 중인 카테고리만 준다. 카테고리를 쓰지 않는 게시판이면 빈 목록이다. (COM-BRD-20)
	@Transactional(readOnly = true)
	public List<BoardCategoryResponse> getActiveCategories(String boardCode) {
		BoardType boardType = getActiveBoardType(boardCode);
		if (!boardType.isUseCategory()) {
			return List.of();
		}
		return boardCategoryRepository.findAllByTypeIdAndActiveTrueOrderBySortOrderAscIdAsc(boardType.getId())
			.stream()
			.map(BoardCategoryResponse::from)
			.toList();
	}

	// 보관된 카테고리까지 준다. 플랫폼 관리자만 본다. (COM-BRD-33)
	@Transactional(readOnly = true)
	public List<AdminBoardCategoryResponse> getAllCategories(Requester requester, String boardCode) {
		BoardType boardType = getBoardType(boardCode);
		return boardCategoryRepository.findAllByTypeIdOrderBySortOrderAscIdAsc(boardType.getId()).stream()
			.map(AdminBoardCategoryResponse::from)
			.toList();
	}

	// 같은 게시판 안에서 이름은 중복될 수 없다. (COM-BRD-21)
	@Transactional
	public AdminBoardCategoryResponse create(Requester requester, String boardCode,
		CreateBoardCategoryRequest request) {
		BoardType boardType = getBoardType(boardCode);
		if (boardCategoryRepository.existsByTypeIdAndName(boardType.getId(), request.name())) {
			throw new BusinessException(ErrorCode.DUPLICATE_CATEGORY_NAME);
		}
		BoardCategory category = BoardCategory.create(
			boardType.getId(), request.name(), request.sortOrder(), requester.accountUuid());
		saveRejectingDuplicateName(category);
		return AdminBoardCategoryResponse.from(category);
	}

	// 이름과 순서만 바꾼다. 자기 자신의 이름은 중복으로 보지 않는다. (COM-BRD-21)
	@Transactional
	public AdminBoardCategoryResponse update(Requester requester, UUID categoryUuid,
		UpdateBoardCategoryRequest request) {
		BoardCategory category = getCategory(categoryUuid);
		if (boardCategoryRepository.existsByTypeIdAndNameAndIdNot(
			category.getTypeId(), request.name(), category.getId())) {
			throw new BusinessException(ErrorCode.DUPLICATE_CATEGORY_NAME);
		}
		category.update(request.name(), request.sortOrder(), requester.accountUuid());
		saveRejectingDuplicateName(category);
		return AdminBoardCategoryResponse.from(category);
	}

	// 삭제하지 않고 보관한다. (COM-BRD-22)
	@Transactional
	public void archive(Requester requester, UUID categoryUuid) {
		getCategory(categoryUuid).archive(requester.accountUuid());
	}

	// 보관한 카테고리를 다시 쓴다. (COM-BRD-33)
	@Transactional
	public void restore(Requester requester, UUID categoryUuid) {
		getCategory(categoryUuid).restore(requester.accountUuid());
	}

	// 동시에 같은 이름으로 저장하면 사전 확인을 지나칠 수 있다. 그때 UNIQUE(typeId, name) 위반을 409로 바꾼다.
	// 이 테이블의 다른 제약(uuid, typeId 외래키)은 서버가 채우는 값이라 여기서 위반될 일이 없다.
	private void saveRejectingDuplicateName(BoardCategory category) {
		try {
			boardCategoryRepository.saveAndFlush(category);
		} catch (DataIntegrityViolationException exception) {
			throw new BusinessException(ErrorCode.DUPLICATE_CATEGORY_NAME);
		}
	}

	private BoardType getActiveBoardType(String boardCode) {
		BoardType boardType = getBoardType(boardCode);
		if (!boardType.isActive()) {
			throw new BusinessException(ErrorCode.BOARD_NOT_FOUND);
		}
		return boardType;
	}

	private BoardType getBoardType(String boardCode) {
		return boardTypeRepository.findByCode(boardCode)
			.orElseThrow(() -> new BusinessException(ErrorCode.BOARD_NOT_FOUND));
	}

	private BoardCategory getCategory(UUID categoryUuid) {
		return boardCategoryRepository.findByUuid(categoryUuid)
			.orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
	}
}
