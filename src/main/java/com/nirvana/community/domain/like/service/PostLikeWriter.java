package com.nirvana.community.domain.like.service;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nirvana.community.domain.like.entity.PostLike;
import com.nirvana.community.domain.like.repository.PostLikeRepository;

import lombok.RequiredArgsConstructor;

// 좋아요 행을 켜고 끄는 MySQL 트랜잭션만 맡는다. 돌려주는 값은 "상태가 실제로 바뀌었는지"이고 좋아요 수는 true일 때만 바꾼다.
// PostLikeService 안의 메서드로 두면 같은 클래스 안 호출이라 @Transactional이 걸리지 않아(self-invocation) 클래스를 나눴다.
@Component
@RequiredArgsConstructor
public class PostLikeWriter {

	private final PostLikeRepository postLikeRepository;

	// 행이 없으면 켜진 행을 새로 넣는다. 있으면 잠그고 다시 켠다(취소 후 다시 누르면 같은 행). (COM-LIK-02, COM-LIK-04)
	// 같은 회원이 동시에 처음 누르면 늦은 쪽 INSERT가 UNIQUE(postId, userUuid) 위반으로
	// DataIntegrityViolationException을 던진다. 이 트랜잭션은 롤백되고 PostLikeService가 "이미 누른 상태"로 처리한다.
	@Transactional
	public boolean like(Long postId, UUID userUuid) {
		if (!postLikeRepository.existsByPostIdAndUserUuid(postId, userUuid)) {
			postLikeRepository.saveAndFlush(PostLike.create(postId, userUuid));
			return true;
		}
		return postLikeRepository.findWithLockByPostIdAndUserUuid(postId, userUuid)
			.map(PostLike::like)
			.orElse(false);
	}

	// 행을 잠그고 끈다. 행이 없거나 이미 꺼져 있으면 바뀐 것이 없다. 행은 지우지 않는다. (COM-LIK-02, COM-LIK-04)
	@Transactional
	public boolean unlike(Long postId, UUID userUuid) {
		return postLikeRepository.findWithLockByPostIdAndUserUuid(postId, userUuid)
			.map(PostLike::unlike)
			.orElse(false);
	}
}
