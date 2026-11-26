package com.nirvana.community.domain.board.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nirvana.community.domain.board.entity.BoardCategory;
import com.nirvana.community.domain.board.entity.BoardType;
import com.nirvana.community.domain.board.repository.BoardCategoryRepository;
import com.nirvana.community.domain.board.repository.BoardTypeRepository;
import com.nirvana.community.global.exception.BusinessException;
import com.nirvana.community.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

// 다른 도메인(글·댓글 등)이 게시판·카테고리를 읽을 때 쓴다. 다른 도메인은 board의 Repository를 직접 부르지 않는다.
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardReader {

	private final BoardTypeRepository boardTypeRepository;
	private final BoardCategoryRepository boardCategoryRepository;

	// 단건 조회: 글 작성에서 쓴다. 비활성 게시판은 새 글을 받지 않으므로 없는 게시판과 같이 404로 본다. (COM-BRD-12)
	public BoardType getActiveBoardType(String boardCode) {
		return boardTypeRepository.findByCode(boardCode)
			.filter(BoardType::isActive)
			.orElseThrow(() -> new BusinessException(ErrorCode.BOARD_NOT_FOUND));
	}

	// 단건 조회: 글 작성에서 고른 카테고리를 읽는다. 쓸 수 있는 카테고리인지는 부르는 쪽이 판단한다.
	public Optional<BoardCategory> findCategory(UUID categoryUuid) {
		return boardCategoryRepository.findByUuid(categoryUuid);
	}
}
