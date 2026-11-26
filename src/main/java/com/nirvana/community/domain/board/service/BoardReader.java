package com.nirvana.community.domain.board.service;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.nirvana.community.domain.board.entity.BoardCategory;
import com.nirvana.community.domain.board.entity.BoardType;
import com.nirvana.community.domain.board.repository.BoardCategoryRepository;
import com.nirvana.community.domain.board.repository.BoardTypeRepository;
import com.nirvana.community.global.exception.BusinessException;
import com.nirvana.community.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

// 다른 도메인(글 등)이 게시판·카테고리를 읽을 때 쓴다. 다른 도메인은 board의 Repository를 직접 부르지 않는다.
// 잠금 조회는 잠금이 트랜잭션이 끝날 때 풀리므로, 확인과 저장을 하는 바깥 트랜잭션 안에서만 부를 수 있게
// 메서드마다 MANDATORY를 붙인다(트랜잭션 없이 부르면 바로 예외).
// 잠금 없는 조회는 트랜잭션을 요구하지 않는다. 바깥 트랜잭션이 있으면 그 안에서, 없으면 Repository 기본 트랜잭션으로 읽는다.
@Component
@RequiredArgsConstructor
public class BoardReader {

	private final BoardTypeRepository boardTypeRepository;
	private final BoardCategoryRepository boardCategoryRepository;

	// 단건 조회: 글 작성에서 쓴다. 게시판 행에 공유 잠금을 걸고 읽는다.
	// 비활성 게시판은 새 글을 받지 않으므로 없는 게시판과 같이 404로 본다. (COM-BRD-12)
	@Transactional(propagation = Propagation.MANDATORY)
	public BoardType getActiveBoardTypeWithShareLock(String boardCode) {
		return boardTypeRepository.findByCodeWithShareLock(boardCode)
			.filter(BoardType::isActive)
			.orElseThrow(() -> new BusinessException(ErrorCode.BOARD_NOT_FOUND));
	}

	// 단건 조회: 글 작성에서 고른 카테고리를 공유 잠금을 걸고 읽는다. 쓸 수 있는 카테고리인지는 부르는 쪽이 판단한다.
	// 교착을 피하려고 게시판을 먼저 잠근 뒤에 부른다(게시판 → 카테고리 순서).
	@Transactional(propagation = Propagation.MANDATORY)
	public Optional<BoardCategory> findCategoryWithShareLock(UUID categoryUuid) {
		return boardCategoryRepository.findByUuidWithShareLock(categoryUuid);
	}

	// 단건 조회: 게시글 목록에서 쓴다. 잠그지 않고 읽는다. 비활성 게시판은 없는 게시판과 같이 404로 본다. (COM-BRD-12)
	public BoardType getActiveBoardType(String boardCode) {
		return boardTypeRepository.findByCode(boardCode)
			.filter(BoardType::isActive)
			.orElseThrow(() -> new BusinessException(ErrorCode.BOARD_NOT_FOUND));
	}

	// 단건 조회: 게시글 목록의 카테고리 필터에서 쓴다. 잠그지 않고 읽는다. 걸러 볼 수 있는지는 부르는 쪽이 판단한다.
	public Optional<BoardCategory> findCategory(UUID categoryUuid) {
		return boardCategoryRepository.findByUuid(categoryUuid);
	}

	// 다건 조회: 게시글 목록 한 페이지의 카테고리를 한 번의 쿼리로 읽어 id별 Map으로 준다. 글마다 따로 읽지 않으려고 쓴다.
	public Map<Long, BoardCategory> findCategoriesByIds(Collection<Long> categoryIds) {
		if (categoryIds.isEmpty()) {
			return Map.of();
		}
		return boardCategoryRepository.findAllById(categoryIds).stream()
			.collect(Collectors.toMap(BoardCategory::getId, Function.identity()));
	}
}
