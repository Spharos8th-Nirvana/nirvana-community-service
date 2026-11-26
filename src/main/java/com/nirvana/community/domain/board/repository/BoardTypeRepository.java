package com.nirvana.community.domain.board.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nirvana.community.domain.board.entity.BoardType;

import jakarta.persistence.LockModeType;

public interface BoardTypeRepository extends JpaRepository<BoardType, Long> {

	// 운영 중인 게시판만, 등록한 순서로. (COM-BRD-10)
	List<BoardType> findAllByActiveTrueOrderByIdAsc();

	Optional<BoardType> findByCode(String code);

	boolean existsByCode(String code);

	// 단건 조회: 글 작성에서만 쓴다. 공유 잠금(MySQL FOR SHARE)을 걸어, 작성 트랜잭션이 끝날 때까지
	// 관리자가 이 게시판을 비활성화(UPDATE)하지 못하게 한다. 목록·상세 같은 다른 조회는 findByCode를 쓴다. (COM-BRD-12)
	@Lock(LockModeType.PESSIMISTIC_READ)
	@Query("select b from BoardType b where b.code = :code")
	Optional<BoardType> findByCodeWithShareLock(@Param("code") String code);
}
