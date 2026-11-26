package com.nirvana.community.domain.post.service;

import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nirvana.community.domain.board.entity.BoardType;
import com.nirvana.community.domain.board.service.BoardPermissionChecker;
import com.nirvana.community.domain.board.service.BoardReader;
import com.nirvana.community.domain.board.entity.BoardCategory;
import com.nirvana.community.domain.post.dto.PostDetailRow;
import com.nirvana.community.domain.post.dto.PostSummaryRow;
import com.nirvana.community.domain.post.entity.Post;
import com.nirvana.community.domain.post.repository.PostRepository;
import com.nirvana.community.global.auth.Requester;
import com.nirvana.community.global.exception.BusinessException;
import com.nirvana.community.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

// 게시글 조회 중 MySQL을 읽는 부분만 맡는다. 게시판 확인, 열람 권한 확인, 글 조회를 한 읽기 트랜잭션에서 한다.
// MongoDB 집계 수는 여기서 읽지 않는다. PostService가 이 트랜잭션이 끝난 뒤 읽어 DB 커넥션을 붙잡지 않게 한다.
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostQueryService {

	// 공개 시각 최신순, 같으면 나중에 쓴 글(id 큰 것) 먼저. 클라이언트의 sort는 받지 않는다. (COM-PST-14, COM-PST-20)
	private static final Sort LATEST_FIRST = Sort.by(Sort.Order.desc("scheduledAt"), Sort.Order.desc("id"));

	private final PostRepository postRepository;
	private final BoardReader boardReader;
	private final BoardPermissionChecker boardPermissionChecker;

	// 다건 조회: 게시판 글 목록 한 페이지를 읽는다. 게시판 확인 → 열람 권한 확인 → 카테고리 확인 → 조회.
	// 공개 상태이고 숨김·삭제되지 않은 글만 준다. REVIEW 게시판도 막지 않는다. (COM-PST-20, COM-BRD-01, 4.5)
	public Page<PostSummaryRow> listVisiblePosts(String boardCode, @Nullable Requester requester,
		@Nullable UUID categoryUuid, Pageable pageable) {
		BoardType boardType = boardReader.getActiveBoardType(boardCode);
		boardPermissionChecker.validateReadable(boardType.getId(), requester);
		Long categoryId = resolveCategoryFilter(boardType, categoryUuid);
		PageRequest latestFirst = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), LATEST_FIRST);
		return postRepository.findVisibleSummaries(boardType.getId(), categoryId, latestFirst);
	}

	// 단건 조회: 게시글 상세. 글 조회(없는 글·남의 임시저장·예약 글은 404) → 게시판 확인 → 열람 권한 확인 → 카테고리.
	// 숨김·삭제 글도 여기서는 돌려준다. 본문을 보여 줄지는 PostService가 Post에 물어 정한다. (COM-PST-23, COM-BRD-01, 4.5)
	public PostDetailRow getVisiblePost(UUID postUuid, @Nullable Requester requester) {
		Post post = postRepository.findByUuid(postUuid)
			.filter(found -> found.isVisibleTo(requester))
			.orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
		BoardType boardType = boardReader.getActiveBoardTypeById(post.getTypeId());
		boardPermissionChecker.validateReadable(boardType.getId(), requester);
		return new PostDetailRow(post, boardType.getCode(), findCategoryOf(post));
	}

	private @Nullable BoardCategory findCategoryOf(Post post) {
		if (post.getCategoryId() == null) {
			return null;
		}
		return boardReader.findCategoryById(post.getCategoryId()).orElse(null);
	}

	// 카테고리를 주지 않으면 거르지 않는다. 없는 것, 다른 게시판 것은 INVALID_CATEGORY(400)로 거절한다.
	// 보관된 카테고리는 기존 글이 그대로 보이므로(COM-BRD-22) 걸러 보는 것도 막지 않는다.
	private @Nullable Long resolveCategoryFilter(BoardType boardType, @Nullable UUID categoryUuid) {
		if (categoryUuid == null) {
			return null;
		}
		return boardReader.findCategory(categoryUuid)
			.filter(category -> category.belongsTo(boardType))
			.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CATEGORY))
			.getId();
	}
}
