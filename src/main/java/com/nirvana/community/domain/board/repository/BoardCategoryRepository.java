package com.nirvana.community.domain.board.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nirvana.community.domain.board.entity.BoardCategory;

public interface BoardCategoryRepository extends JpaRepository<BoardCategory, Long> {

	// 사용자용: 사용 중인 카테고리만, 순서가 같으면 등록한 순서로. (COM-BRD-20)
	List<BoardCategory> findAllByTypeIdAndActiveTrueOrderBySortOrderAscIdAsc(Long typeId);

	// 관리용: 보관된 카테고리까지. (COM-BRD-33)
	List<BoardCategory> findAllByTypeIdOrderBySortOrderAscIdAsc(Long typeId);

	Optional<BoardCategory> findByUuid(UUID uuid);

	boolean existsByTypeIdAndName(Long typeId, String name);

	// 수정할 때 자기 자신은 중복으로 보지 않는다. (COM-BRD-21)
	boolean existsByTypeIdAndNameAndIdNot(Long typeId, String name, Long id);
}
