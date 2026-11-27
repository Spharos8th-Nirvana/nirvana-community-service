package com.nirvana.community.domain.post.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.nirvana.community.domain.board.entity.BoardCategory;
import com.nirvana.community.domain.board.entity.BoardType;
import com.nirvana.community.domain.board.service.BoardReader;
import com.nirvana.community.domain.post.dto.CreatePostRequest;
import com.nirvana.community.domain.post.dto.CreatePostResponse;
import com.nirvana.community.domain.post.dto.MyPostSummaryResponse;
import com.nirvana.community.domain.post.dto.MyPostSummaryRow;
import com.nirvana.community.domain.post.dto.PostDetailResponse;
import com.nirvana.community.domain.post.dto.PostDetailRow;
import com.nirvana.community.domain.post.dto.PostSummaryResponse;
import com.nirvana.community.domain.post.dto.PostSummaryRow;
import com.nirvana.community.domain.post.dto.UpdatePostRequest;
import com.nirvana.community.domain.post.dto.UpdatePostResponse;
import com.nirvana.community.domain.post.entity.Post;
import com.nirvana.community.domain.post.entity.PostStatus;
import com.nirvana.community.domain.post.stats.dto.PostCounts;
import com.nirvana.community.domain.post.stats.service.PostStatsReader;
import com.nirvana.community.global.auth.Requester;
import com.nirvana.community.global.response.PageResponse;

import lombok.RequiredArgsConstructor;

// 이 클래스에는 일부러 @Transactional을 붙이지 않는다. 트랜잭션은 PostWriter, PostQueryService 안에서만 열린다.
@Service
@RequiredArgsConstructor
public class PostService {

	private final PostContentSanitizer postContentSanitizer;
	private final AuthorNameProvider authorNameProvider;
	private final PostWriter postWriter;
	private final PostQueryService postQueryService;
	private final BoardReader boardReader;
	private final PostStatsReader postStatsReader;

	// 본문 정제 → 작성자 이름 조회 → (트랜잭션) 확인과 저장. 임시저장도 같은 순서다. (COM-PST-01, COM-PST-04, COM-PST-05)
	// 작성자 이름은 나중에 Member·Temple 원격 호출로 바뀐다. 그동안 DB 커넥션을 붙잡지 않도록 트랜잭션 전에 구한다.
	public CreatePostResponse create(Requester requester, String boardCode, CreatePostRequest request) {
		String content = postContentSanitizer.sanitize(request.content());
		String authorName = authorNameProvider.getAuthorName(requester);
		Post post = postWriter.create(boardCode, requester, authorName, request.title(), content,
			request.categoryUuid(), request.statusOrDefault());
		return CreatePostResponse.from(post);
	}

	// 본문 정제 → (트랜잭션) 확인과 수정. 정제는 DB를 쓰지 않아 트랜잭션 전에 한다. (COM-PST-08, COM-PST-04)
	public UpdatePostResponse update(Requester requester, UUID postUuid, UpdatePostRequest request) {
		String content = postContentSanitizer.sanitize(request.content());
		Post post = postWriter.update(postUuid, requester, request.title(), content, request.categoryUuid());
		return UpdatePostResponse.from(post);
	}

	// (트랜잭션) 확인과 공개. 응답은 수정과 같이 식별자·제목·상태만 준다. (COM-PST-05, COM-PST-12)
	public UpdatePostResponse publish(Requester requester, UUID postUuid) {
		Post post = postWriter.publish(postUuid, requester);
		return UpdatePostResponse.from(post);
	}

	// (트랜잭션) 확인과 삭제 표시. (COM-PST-09, COM-PST-15)
	public void delete(Requester requester, UUID postUuid) {
		postWriter.delete(postUuid, requester);
	}

	// 다건 조회: 게시판 글 목록. (트랜잭션) 글 한 페이지 → 카테고리 한 번에 → 집계 수 한 번에 → 응답. (COM-PST-20, COM-PST-21)
	// 카테고리와 MongoDB 집계 수는 글 조회 트랜잭션이 끝난 뒤 읽는다. MongoDB가 늦거나 꺼져도 DB 커넥션을 붙잡지 않는다.
	// 카테고리는 지우지 않고 보관만 하므로(COM-BRD-22) 트랜잭션 밖에서 따로 읽어도 이름을 못 찾을 일이 없다.
	public PageResponse<PostSummaryResponse> listPosts(String boardCode, @Nullable Requester requester,
		@Nullable UUID categoryUuid, Pageable pageable) {
		Page<PostSummaryRow> posts = postQueryService.listVisiblePosts(boardCode, requester, categoryUuid, pageable);
		Map<Long, BoardCategory> categories = boardReader.findCategoriesByIds(categoryIdsOf(posts.getContent()));
		Map<UUID, PostCounts> counts = postStatsReader.getCountsByPostUuids(postUuidsOf(posts.getContent()));
		return PageResponse.from(posts.map(post ->
			PostSummaryResponse.of(post, categoryOf(post, categories),
				counts.getOrDefault(post.postUuid(), PostCounts.ZERO))));
	}

	// 다건 조회: 내가 쓴 글 목록. (트랜잭션) 내 글 한 페이지 → 게시판·카테고리 한 번에 → 집계 수 한 번에 → 응답. (COM-PST-25)
	// 게시판 목록과 같은 이유로 게시판·카테고리·집계 수는 글 조회 트랜잭션이 끝난 뒤 읽는다.
	// 게시판은 지우지 않고 비활성화만 하므로(COM-BRD-12) 글의 게시판을 못 찾을 일이 없다.
	public PageResponse<MyPostSummaryResponse> listMyPosts(Requester requester, @Nullable PostStatus status,
		Pageable pageable) {
		Page<MyPostSummaryRow> posts = postQueryService.listMyPosts(requester, status, pageable);
		List<MyPostSummaryRow> rows = posts.getContent();
		Map<Long, BoardType> boards = boardReader.findBoardTypesByIds(
			rows.stream().map(MyPostSummaryRow::typeId).distinct().toList());
		Map<Long, BoardCategory> categories = boardReader.findCategoriesByIds(
			rows.stream().map(MyPostSummaryRow::categoryId).filter(Objects::nonNull).distinct().toList());
		Map<UUID, PostCounts> counts = postStatsReader.getCountsByPostUuids(
			rows.stream().map(MyPostSummaryRow::postUuid).toList());
		return PageResponse.from(posts.map(post ->
			MyPostSummaryResponse.of(post, boards.get(post.typeId()).getCode(),
				post.categoryId() == null ? null : categories.get(post.categoryId()),
				counts.getOrDefault(post.postUuid(), PostCounts.ZERO))));
	}

	// 단건 조회: 게시글 상세. (트랜잭션) 글·게시판·권한·카테고리 → 본문을 볼 수 있는지 → 집계 수 → 응답. (COM-PST-23, COM-PST-24)
	// 숨김·삭제로 볼 수 없는 글은 집계 수도 가리므로 MongoDB를 읽지 않는다.
	// 집계 수는 트랜잭션이 끝난 뒤 읽는다. MongoDB가 늦거나 꺼져도 DB 커넥션을 붙잡지 않는다. 조회수는 올리지 않는다.
	public PostDetailResponse getPost(UUID postUuid, @Nullable Requester requester) {
		PostDetailRow row = postQueryService.getVisiblePost(postUuid, requester);
		if (!row.post().isContentViewableBy(requester)) {
			return PostDetailResponse.unviewable(postUuid);
		}
		PostCounts counts = postStatsReader.getCounts(postUuid);
		return PostDetailResponse.of(row, counts, requester);
	}

	// 카테고리 없는 글은 null. 불변 Map(Map.of)은 get(null)에서 예외를 던지므로 먼저 거른다.
	private @Nullable BoardCategory categoryOf(PostSummaryRow post, Map<Long, BoardCategory> categories) {
		return post.categoryId() == null ? null : categories.get(post.categoryId());
	}

	private List<Long> categoryIdsOf(List<PostSummaryRow> posts) {
		return posts.stream().map(PostSummaryRow::categoryId).filter(Objects::nonNull).distinct().toList();
	}

	private List<UUID> postUuidsOf(List<PostSummaryRow> posts) {
		return posts.stream().map(PostSummaryRow::postUuid).toList();
	}
}
