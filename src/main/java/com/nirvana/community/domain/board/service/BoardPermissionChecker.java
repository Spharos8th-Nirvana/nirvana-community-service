package com.nirvana.community.domain.board.service;

import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nirvana.community.domain.board.entity.BoardPermission;
import com.nirvana.community.domain.board.entity.BoardPermissionId;
import com.nirvana.community.domain.board.entity.BoardRole;
import com.nirvana.community.domain.board.repository.BoardPermissionRepository;
import com.nirvana.community.global.auth.Requester;
import com.nirvana.community.global.exception.BusinessException;
import com.nirvana.community.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

// 글·댓글 API가 게시판 종류와 요청자로 열람·작성 권한을 확인할 때 쓴다. (COM-BRD-01)
// 요청자가 null이면 비회원(GUEST)으로 보고, 권한 행이 없으면 열람·작성 모두 거절한다.
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardPermissionChecker {

	private final BoardPermissionRepository boardPermissionRepository;

	public boolean canRead(Long boardTypeId, @Nullable Requester requester) {
		return findPermission(boardTypeId, requester).map(BoardPermission::isCanRead).orElse(false);
	}

	public boolean canWrite(Long boardTypeId, @Nullable Requester requester) {
		return findPermission(boardTypeId, requester).map(BoardPermission::isCanWrite).orElse(false);
	}

	public void validateReadable(Long boardTypeId, @Nullable Requester requester) {
		if (!canRead(boardTypeId, requester)) {
			throw new BusinessException(ErrorCode.FORBIDDEN);
		}
	}

	public void validateWritable(Long boardTypeId, @Nullable Requester requester) {
		if (!canWrite(boardTypeId, requester)) {
			throw new BusinessException(ErrorCode.FORBIDDEN);
		}
	}

	private Optional<BoardPermission> findPermission(Long boardTypeId, @Nullable Requester requester) {
		return boardPermissionRepository.findById(new BoardPermissionId(boardTypeId, BoardRole.from(requester)));
	}
}
