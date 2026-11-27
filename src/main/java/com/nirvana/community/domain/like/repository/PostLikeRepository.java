package com.nirvana.community.domain.like.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nirvana.community.domain.like.entity.PostLike;

import jakarta.persistence.LockModeType;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

	// 단건 조회: 좋아요에서 이 회원의 행이 이미 있는지 잠그지 않고 본다. 없으면 새 행을 넣는다. (COM-LIK-04)
	// 없는 행을 잠금 조회하면 MySQL이 빈 구간(gap)을 잠가, 동시에 처음 누른 두 요청이 서로의 INSERT를 막아 교착이 난다.
	boolean existsByPostIdAndUserUuid(Long postId, UUID userUuid);

	// 단건 조회: 이 글에 켜져 있는 좋아요 수를 원장에서 센다. 바뀐 것이 없는 요청과 MongoDB 좋아요 수 반영이 실패한 요청의 응답에 쓴다. (COM-LIK-05)
	long countByPostIdAndLikedTrue(Long postId);

	// 단건 조회: 좋아요·취소에서 이 회원의 행을 배타 잠금(MySQL FOR UPDATE)을 걸고 읽는다.
	// 같은 회원의 요청이 동시에 와도 한 요청씩 liked를 확인하고 바꿔, 좋아요 수가 두 번 바뀌지 않게 한다. (COM-LIK-04, COM-NFR-03)
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select l from PostLike l where l.postId = :postId and l.userUuid = :userUuid")
	Optional<PostLike> findWithLockByPostIdAndUserUuid(@Param("postId") Long postId,
		@Param("userUuid") UUID userUuid);
}
