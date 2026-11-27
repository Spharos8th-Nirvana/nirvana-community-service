package com.nirvana.community.domain.comment.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nirvana.community.domain.comment.dto.CommentResponse;
import com.nirvana.community.domain.comment.dto.CreateCommentRequest;
import com.nirvana.community.domain.comment.dto.CreateCommentResponse;
import com.nirvana.community.domain.comment.entity.Comment;
import com.nirvana.community.domain.comment.repository.CommentRepository;
import com.nirvana.community.domain.post.entity.Post;
import com.nirvana.community.domain.post.service.PostQueryService;
import com.nirvana.community.global.auth.Requester;
import com.nirvana.community.global.author.AuthorNameProvider;
import com.nirvana.community.global.response.PageResponse;

import lombok.RequiredArgsConstructor;

// 댓글 API의 순서를 조립한다. 작성은 트랜잭션 밖에서 작성자 이름을 구한 뒤 CommentWriter에 넘긴다.
// 목록은 외부 호출이 없어 메서드에 바로 읽기 트랜잭션을 건다.
@Service
@RequiredArgsConstructor
public class CommentService {

	// 원댓글은 작성순, 같은 시각이면 먼저 쓴 것(id 작은 것) 먼저. 클라이언트의 sort는 받지 않는다. (COM-CMT-03)
	private static final Sort OLDEST_FIRST = Sort.by(Sort.Order.asc("createdAt"), Sort.Order.asc("id"));

	private final AuthorNameProvider authorNameProvider;
	private final CommentWriter commentWriter;
	private final PostQueryService postQueryService;
	private final CommentRepository commentRepository;

	// 작성자 이름 조회 → (트랜잭션) 확인과 저장. (COM-CMT-01, COM-CMT-02, COM-INT-01)
	// 작성자 이름은 나중에 Member·Temple 원격 호출로 바뀐다. 그동안 DB 커넥션을 붙잡지 않도록 트랜잭션 전에 구한다.
	public CreateCommentResponse create(Requester requester, UUID postUuid, CreateCommentRequest request) {
		String authorName = authorNameProvider.getAuthorName(requester);
		Comment comment = commentWriter.create(postUuid, requester, authorName, request.content(),
			request.parentCommentUuid());
		return CreateCommentResponse.from(comment);
	}

	// 다건 조회: 댓글 목록. 글 확인(게시글 상세와 같은 기준) → 원댓글 한 페이지 → 답글 한 번에 → 답글을 부모 아래에 묶기.
	// 글 본문을 볼 수 없는 글(숨김·삭제)은 댓글도 가리므로 빈 페이지를 준다. (COM-CMT-03, COM-CMT-06, COM-CMT-09, 4.5)
	@Transactional(readOnly = true)
	public PageResponse<CommentResponse> list(UUID postUuid, @Nullable Requester requester, Pageable pageable) {
		Post post = postQueryService.getVisiblePost(postUuid, requester).post();
		PageRequest oldestFirst = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), OLDEST_FIRST);
		if (!post.isContentViewableBy(requester)) {
			return PageResponse.from(Page.empty(oldestFirst));
		}
		Page<Comment> roots = commentRepository.findRootsInList(post.getId(), oldestFirst);
		Map<Long, List<Comment>> repliesByParent = findRepliesByParent(roots.getContent());
		return PageResponse.from(roots.map(root ->
			CommentResponse.of(root, buildReplies(root, repliesByParent, requester), requester)));
	}

	private Map<Long, List<Comment>> findRepliesByParent(List<Comment> roots) {
		if (roots.isEmpty()) {
			return Map.of();
		}
		List<Long> rootIds = roots.stream().map(Comment::getId).toList();
		return commentRepository.findRepliesOfRoots(rootIds).stream()
			.collect(Collectors.groupingBy(Comment::getParentId));
	}

	// 부모의 답글을 작성순으로 만든다. 삭제·숨김 답글은 그 아래 남는 답글이 있을 때만 자리를 남기고, 없으면 뺀다. (COM-CMT-06)
	private List<CommentResponse> buildReplies(Comment parent, Map<Long, List<Comment>> repliesByParent,
		@Nullable Requester requester) {
		List<CommentResponse> replies = new ArrayList<>();
		for (Comment reply : repliesByParent.getOrDefault(parent.getId(), List.of())) {
			List<CommentResponse> children = buildReplies(reply, repliesByParent, requester);
			if (reply.isRemoved() && children.isEmpty()) {
				continue;
			}
			replies.add(CommentResponse.of(reply, children, requester));
		}
		return replies;
	}
}
