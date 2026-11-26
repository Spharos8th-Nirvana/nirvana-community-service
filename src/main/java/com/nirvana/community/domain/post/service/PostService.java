package com.nirvana.community.domain.post.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nirvana.community.domain.board.entity.BoardType;
import com.nirvana.community.domain.board.repository.BoardCategoryRepository;
import com.nirvana.community.domain.board.repository.BoardTypeRepository;
import com.nirvana.community.domain.board.service.BoardPermissionChecker;
import com.nirvana.community.domain.post.dto.CreatePostRequest;
import com.nirvana.community.domain.post.dto.CreatePostResponse;
import com.nirvana.community.domain.post.entity.Post;
import com.nirvana.community.domain.post.repository.PostRepository;
import com.nirvana.community.global.auth.Requester;
import com.nirvana.community.global.exception.BusinessException;
import com.nirvana.community.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {

	private final PostRepository postRepository;
	private final BoardTypeRepository boardTypeRepository;
	private final BoardCategoryRepository boardCategoryRepository;
	private final BoardPermissionChecker boardPermissionChecker;
	private final AuthorNameProvider authorNameProvider;
	private final PostContentSanitizer postContentSanitizer;

	// 본문 정제 → 게시판 확인 → 작성 권한 확인 → 카테고리 확인 → 글 생성 → 저장. (COM-PST-01, COM-PST-04)
	@Transactional
	public CreatePostResponse create(Requester requester, String boardCode, CreatePostRequest request) {
		String content = postContentSanitizer.sanitize(request.content());
		BoardType boardType = getWritableBoardType(boardCode);
		boardPermissionChecker.validateWritable(boardType.getId(), requester);
		Long categoryId = resolveCategoryId(boardType, request.categoryUuid());

		Post post = Post.create(boardType.getId(), categoryId, requester,
			authorNameProvider.getAuthorName(requester), request.title(), content, publishedNow());
		postRepository.save(post);
		return CreatePostResponse.from(post);
	}

	// posts.scheduledAt은 DATETIME(초 단위)이라 MySQL이 소수 초를 반올림한다. 응답 값과 저장 값이 같도록 초 단위로 자른다.
	private LocalDateTime publishedNow() {
		return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
	}

	// 비활성 게시판은 새 글을 받지 않고(COM-BRD-12), 리뷰 게시판은 일반 글로 쓸 수 없다(COM-PST-11).
	private BoardType getWritableBoardType(String boardCode) {
		BoardType boardType = boardTypeRepository.findByCode(boardCode)
			.filter(BoardType::isActive)
			.orElseThrow(() -> new BusinessException(ErrorCode.BOARD_NOT_FOUND));
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
		return boardCategoryRepository.findByUuid(categoryUuid)
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
