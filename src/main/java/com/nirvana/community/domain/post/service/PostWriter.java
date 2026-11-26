package com.nirvana.community.domain.post.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nirvana.community.domain.board.entity.BoardType;
import com.nirvana.community.domain.board.service.BoardPermissionChecker;
import com.nirvana.community.domain.board.service.BoardReader;
import com.nirvana.community.domain.post.entity.Post;
import com.nirvana.community.domain.post.repository.PostRepository;
import com.nirvana.community.global.auth.Requester;
import com.nirvana.community.global.exception.BusinessException;
import com.nirvana.community.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

// 게시글 작성 중 DB 트랜잭션이 필요한 부분(확인과 저장)만 맡는다. PostService가 준비를 끝낸 뒤 부른다.
// 게시판·카테고리를 잠근 채 확인하고 같은 트랜잭션에서 저장해야 확인과 저장 사이에 상태가 바뀌지 않는다.
// PostService 안의 메서드로 두면 같은 클래스 안 호출이라 @Transactional이 걸리지 않아(self-invocation) 클래스를 나눴다.
@Component
@RequiredArgsConstructor
public class PostWriter {

	private final PostRepository postRepository;
	private final BoardReader boardReader;
	private final BoardPermissionChecker boardPermissionChecker;

	// 게시판 잠금·확인 → 작성 권한 확인 → 카테고리 잠금·확인 → 글 생성 → 저장. (COM-PST-01)
	@Transactional
	public Post create(String boardCode, Requester author, String authorName, String title, String content,
		@Nullable UUID categoryUuid) {
		BoardType boardType = getWritableBoardType(boardCode);
		boardPermissionChecker.validateWritable(boardType.getId(), author);
		Long categoryId = resolveCategoryId(boardType, categoryUuid);

		Post post = Post.create(boardType.getId(), categoryId, author, authorName, title, content, publishedNow());
		return postRepository.save(post);
	}

	// posts.scheduledAt은 DATETIME(초 단위)이라 MySQL이 소수 초를 반올림한다. 응답 값과 저장 값이 같도록 초 단위로 자른다.
	private LocalDateTime publishedNow() {
		return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
	}

	// 운영 중인 게시판이어야 하고(COM-BRD-12), 리뷰 게시판은 일반 글로 쓸 수 없다(COM-PST-11).
	private BoardType getWritableBoardType(String boardCode) {
		BoardType boardType = boardReader.getActiveBoardTypeWithShareLock(boardCode);
		if (boardType.isReview()) {
			throw new BusinessException(ErrorCode.REVIEW_BOARD_WRITE_NOT_ALLOWED);
		}
		return boardType;
	}

	// 카테고리를 쓰는 게시판은 그 게시판의 사용 중인 카테고리를 꼭 고르고, 쓰지 않는 게시판은 카테고리를 받지 않는다.
	// 없는 것, 다른 게시판 것, 보관된 것은 구분하지 않고 INVALID_CATEGORY로 거절한다. (COM-PST-02, COM-BRD-22)
	private @Nullable Long resolveCategoryId(BoardType boardType, @Nullable UUID categoryUuid) {
		if (!boardType.isUseCategory()) {
			rejectCategory(categoryUuid);
			return null;
		}
		if (categoryUuid == null) {
			throw new BusinessException(ErrorCode.CATEGORY_REQUIRED);
		}
		return boardReader.findCategoryWithShareLock(categoryUuid)
			.filter(category -> category.isUsableIn(boardType))
			.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CATEGORY))
			.getId();
	}

	private void rejectCategory(@Nullable UUID categoryUuid) {
		if (categoryUuid != null) {
			throw new BusinessException(ErrorCode.CATEGORY_NOT_ALLOWED);
		}
	}
}
