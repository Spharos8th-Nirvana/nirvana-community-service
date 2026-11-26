package com.nirvana.community.domain.board.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nirvana.community.domain.board.entity.BoardCategory;

import jakarta.persistence.LockModeType;

public interface BoardCategoryRepository extends JpaRepository<BoardCategory, Long> {

	// 사용자용: 사용 중인 카테고리만, 순서가 같으면 등록한 순서로. (COM-BRD-20)
	List<BoardCategory> findAllByTypeIdAndActiveTrueOrderBySortOrderAscIdAsc(Long typeId);

	// 관리용: 보관된 카테고리까지. (COM-BRD-33)
	List<BoardCategory> findAllByTypeIdOrderBySortOrderAscIdAsc(Long typeId);

	Optional<BoardCategory> findByUuid(UUID uuid);

	// 단건 조회: 글 작성에서만 쓴다. 공유 잠금(MySQL FOR SHARE)을 걸어, 작성 트랜잭션이 끝날 때까지
	// 관리자가 이 카테고리를 보관(UPDATE)하지 못하게 한다. 다른 조회는 findByUuid를 쓴다. (COM-BRD-22)
	@Lock(LockModeType.PESSIMISTIC_READ)
	@Query("select c from BoardCategory c where c.uuid = :uuid")
	Optional<BoardCategory> findByUuidWithShareLock(@Param("uuid") UUID uuid);

	boolean existsByTypeIdAndName(Long typeId, String name);

	// 수정할 때 자기 자신은 중복으로 보지 않는다. (COM-BRD-21)
	boolean existsByTypeIdAndNameAndIdNot(Long typeId, String name, Long id);
}
