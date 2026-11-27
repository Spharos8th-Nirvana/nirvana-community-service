package com.nirvana.community.domain.like.service;

import java.util.OptionalLong;
import java.util.UUID;

import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.ConstraintViolationException.ConstraintKind;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.nirvana.community.domain.like.dto.PostLikeResponse;
import com.nirvana.community.domain.like.repository.PostLikeRepository;
import com.nirvana.community.domain.post.entity.Post;
import com.nirvana.community.domain.post.service.PostQueryService;
import com.nirvana.community.domain.post.stats.service.PostStatsWriter;
import com.nirvana.community.global.auth.Requester;

import lombok.RequiredArgsConstructor;

// 이 클래스에는 일부러 @Transactional을 붙이지 않는다. 트랜잭션은 PostQueryService, PostLikeWriter 안에서만 열린다.
// MongoDB 좋아요 수는 MySQL 트랜잭션이 커밋된 뒤에 바꾼다. 커밋되지 않은 좋아요가 수에 들어가지 않고,
// MongoDB가 늦거나 꺼져도 DB 커넥션을 붙잡지 않는다.
@Service
@RequiredArgsConstructor
public class PostLikeService {

	private final PostQueryService postQueryService;
	private final PostLikeWriter postLikeWriter;
	private final PostLikeRepository postLikeRepository;
	private final PostStatsWriter postStatsWriter;

	// (트랜잭션) 글·게시판·권한 확인 → (트랜잭션) 좋아요 켜기 → 켜졌으면 좋아요 수 +1, 이미 켜져 있었으면 원장에서 지금 수를 센다.
	// 여러 번 불러도 결과가 같다. (COM-LIK-01, COM-LIK-04, COM-LIK-05, COM-LIK-12)
	public PostLikeResponse like(Requester requester, UUID postUuid) {
		Post post = postQueryService.getLikeablePost(postUuid, requester);
		boolean changed = likeOnce(post.getId(), requester.accountUuid());
		long likeCount = changed
			? orCountFromLedger(postStatsWriter.increaseLikeCount(postUuid), post.getId())
			: postLikeRepository.countByPostIdAndLikedTrue(post.getId());
		return new PostLikeResponse(likeCount, true);
	}

	// (트랜잭션) 글·게시판·권한 확인 → (트랜잭션) 좋아요 끄기 → 꺼졌으면 좋아요 수 -1, 이미 꺼져 있었으면 원장에서 지금 수를 센다.
	// 누를 때와 같은 확인을 한다. 숨김·삭제 글은 화면에서 취소 버튼에 닿을 수 없고, 숨김이 풀리면 다시 취소할 수 있다.
	// (COM-LIK-02, COM-LIK-04, COM-LIK-05)
	public PostLikeResponse unlike(Requester requester, UUID postUuid) {
		Post post = postQueryService.getLikeablePost(postUuid, requester);
		boolean changed = postLikeWriter.unlike(post.getId(), requester.accountUuid());
		long likeCount = changed
			? orCountFromLedger(postStatsWriter.decreaseLikeCount(postUuid), post.getId())
			: postLikeRepository.countByPostIdAndLikedTrue(post.getId());
		return new PostLikeResponse(likeCount, false);
	}

	// 같은 회원의 요청이 동시에 처음 누르면 한쪽만 행을 넣고 다른 쪽은 UNIQUE 위반이 난다.
	// 먼저 넣은 요청이 켜진 행을 만들었으므로 늦은 쪽은 "이미 누른 상태"(바뀐 것 없음)로 본다. (COM-LIK-04, COM-NFR-03)
	private boolean likeOnce(Long postId, UUID userUuid) {
		try {
			return postLikeWriter.like(postId, userUuid);
		} catch (DataIntegrityViolationException e) {
			if (isUniqueViolation(e)) {
				return false;
			}
			throw e;
		}
	}

	// UNIQUE 위반일 때만 "이미 누른 상태"로 본다. NOT NULL 같은 다른 무결성 오류까지 삼키지 않는다.
	private boolean isUniqueViolation(DataIntegrityViolationException e) {
		return e.getCause() instanceof ConstraintViolationException violation
			&& violation.getKind() == ConstraintKind.UNIQUE;
	}

	// MongoDB 반영이 실패하면 0을 주지 않고 원장(MySQL)에서 켜진 좋아요를 세어 준다. (COM-LIK-05)
	private long orCountFromLedger(OptionalLong likeCount, Long postId) {
		return likeCount.isPresent() ? likeCount.getAsLong() : postLikeRepository.countByPostIdAndLikedTrue(postId);
	}
}
