package com.nirvana.community.domain.board.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nirvana.community.domain.board.entity.BoardType;

public interface BoardTypeRepository extends JpaRepository<BoardType, Long> {

	// 운영 중인 게시판만, 등록한 순서로. (COM-BRD-10)
	List<BoardType> findAllByActiveTrueOrderByIdAsc();

	Optional<BoardType> findByCode(String code);

	boolean existsByCode(String code);
}
