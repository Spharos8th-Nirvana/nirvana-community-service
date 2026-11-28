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
import com.nirvana.community.domain.post.dto.MyPostSummaryRow;
import com.nirvana.community.domain.post.dto.PostDetailRow;
import com.nirvana.community.domain.post.dto.PostSummaryRow;
import com.nirvana.community.domain.post.entity.Post;
import com.nirvana.community.domain.post.entity.PostStatus;
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

	// 최근에 쓴 글 먼저, 같으면 id 큰 것 먼저. 임시저장 글은 공개 시각이 없어 작성 시각으로 정렬한다. (COM-PST-25)
	private static final Sort WRITTEN_LATEST_FIRST = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));

	// 키워드 LIKE 검색의 이스케이프 문자. MySQL 문자열에서 따로 처리되는 '\' 대신 '!'를 쓴다. 쿼리의 escape '!'와 같아야 한다.
	private static final String LIKE_ESCAPE = "!";

	private final PostRepository postRepository;
	private final BoardReader boardReader;
	private final BoardPermissionChecker boardPermissionChecker;

	// 다건 조회: 게시판 글 목록 한 페이지를 읽는다. 게시판 확인 → 열람 권한 확인 → 카테고리 확인 → 조회.
	// 공개 상태이고 숨김·삭제되지 않은 글만 준다. REVIEW 게시판도 막지 않는다. (COM-PST-20, COM-BRD-01, 4.5)
	// keyword를 주면 제목에 그 글자가 들어 있는 글만 준다. null이면 거르지 않는다. (이슈 #47)
	public Page<PostSummaryRow> listVisiblePosts(String boardCode, @Nullable Requester requester,
		@Nullable UUID categoryUuid, @Nullable String keyword, Pageable pageable) {
		BoardType boardType = boardReader.getActiveBoardType(boardCode);
		boardPermissionChecker.validateReadable(boardType.getId(), requester);
		Long categoryId = resolveCategoryFilter(boardType, categoryUuid);
		PageRequest latestFirst = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), LATEST_FIRST);
		return postRepository.findVisibleSummaries(boardType.getId(), categoryId, toContainsPattern(keyword),
			latestFirst);
	}

	// 다건 조회: 내가 쓴 글 목록 한 페이지를 읽는다. 임시저장·예약·공개·숨김 글을 모두 주고 삭제한 글만 뺀다. (COM-PST-25, 4.5)
	// 자기 글이라 게시판 열람 권한은 보지 않고, 비활성 게시판의 글도 준다. 클라이언트의 sort는 받지 않는다.
	public Page<MyPostSummaryRow> listMyPosts(Requester requester, @Nullable PostStatus status, Pageable pageable) {
		PageRequest latestFirst = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), WRITTEN_LATEST_FIRST);
		return postRepository.findMySummaries(requester.accountUuid(), requester.accountType(), status, latestFirst);
	}

	// 단건 조회: 좋아요·취소에서 대상 글을 읽는다. like 도메인은 PostRepository 대신 이 메서드를 쓴다.
	// 글 조회(없는 글·남의 임시저장·예약 글은 404) → 게시판 확인 → 열람 권한 확인 → 좋아요를 쓰는 게시판인지(400)
	// → 좋아요할 수 있는 글인지(내 임시저장·예약 글, 숨김·삭제 글은 409). 권한을 먼저 봐서 못 보는 사람에게 글 상태가 새지 않게 한다.
	// (COM-LIK-01, COM-LIK-12, COM-BRD-01, 4.5)
	public Post getLikeablePost(UUID postUuid, Requester requester) {
		Post post = postRepository.findByUuid(postUuid)
			.filter(found -> found.isVisibleTo(requester))
			.orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
		BoardType boardType = boardReader.getActiveBoardTypeById(post.getTypeId());
		boardPermissionChecker.validateReadable(boardType.getId(), requester);
		if (!boardType.isAllowLike()) {
			throw new BusinessException(ErrorCode.LIKE_NOT_ALLOWED);
		}
		if (!post.isLikeable()) {
			throw new BusinessException(ErrorCode.POST_NOT_LIKEABLE);
		}
		return post;
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

	// 단건 조회: 댓글 작성에서 댓글을 달 글을 읽는다. comment 도메인은 PostRepository 대신 이 메서드를 쓴다.
	// 없는 글·남의 임시저장·예약 글은 404, 내 임시저장·예약 글과 숨김·삭제 글은 댓글을 쓸 수 없어 409다. (COM-CMT-01, 4.5)
	// 게시판 확인과 작성 권한 확인은 부르는 쪽이 한다.
	public Post getCommentablePost(UUID postUuid, Requester requester) {
		Post post = postRepository.findByUuid(postUuid)
			.filter(found -> found.isVisibleTo(requester))
			.orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
		if (!post.isCommentable()) {
			throw new BusinessException(ErrorCode.POST_NOT_COMMENTABLE);
		}
		return post;
	}

	private @Nullable BoardCategory findCategoryOf(Post post) {
		if (post.getCategoryId() == null) {
			return null;
		}
		return boardReader.findCategoryById(post.getCategoryId()).orElse(null);
	}

	// "키워드가 들어 있음" LIKE 패턴을 만든다. 키워드의 %, _, 이스케이프 문자는 글자 그대로 찾도록 앞에 '!'를 붙인다.
	// 이스케이프 문자를 먼저 바꿔야 뒤에서 붙인 '!'가 두 번 바뀌지 않는다.
	private @Nullable String toContainsPattern(@Nullable String keyword) {
		if (keyword == null) {
			return null;
		}
		String escaped = keyword
			.replace(LIKE_ESCAPE, LIKE_ESCAPE + LIKE_ESCAPE)
			.replace("%", LIKE_ESCAPE + "%")
			.replace("_", LIKE_ESCAPE + "_");
		return "%" + escaped + "%";
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
