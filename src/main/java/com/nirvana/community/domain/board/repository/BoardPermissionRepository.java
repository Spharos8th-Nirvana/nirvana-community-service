package com.nirvana.community.domain.board.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nirvana.community.domain.board.entity.BoardPermission;
import com.nirvana.community.domain.board.entity.BoardPermissionId;
import com.nirvana.community.domain.board.entity.BoardRole;

public interface BoardPermissionRepository extends JpaRepository<BoardPermission, BoardPermissionId> {

	// 한 게시판의 역할별 권한. 관리자 조회·수정 응답에 쓴다. (COM-BRD-13)
	List<BoardPermission> findAllByBoardTypeId(Long boardTypeId);

	// 한 역할의 모든 게시판 권한. 게시판 목록에서 게시판마다 따로 조회하지 않으려고 한 번에 읽는다. (COM-BRD-10)
	List<BoardPermission> findAllByRole(BoardRole role);
}
