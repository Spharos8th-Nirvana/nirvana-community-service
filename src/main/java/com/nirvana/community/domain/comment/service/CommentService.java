package com.nirvana.community.domain.comment.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nirvana.community.domain.comment.dto.CreateCommentRequest;
import com.nirvana.community.domain.comment.dto.CreateCommentResponse;
import com.nirvana.community.domain.comment.entity.Comment;
import com.nirvana.community.global.auth.Requester;
import com.nirvana.community.global.author.AuthorNameProvider;

import lombok.RequiredArgsConstructor;

// 댓글 API의 순서를 조립한다. 작성은 트랜잭션 밖에서 작성자 이름을 구한 뒤 CommentWriter에 넘긴다.
@Service
@RequiredArgsConstructor
public class CommentService {

	private final AuthorNameProvider authorNameProvider;
	private final CommentWriter commentWriter;

	// 작성자 이름 조회 → (트랜잭션) 확인과 저장. (COM-CMT-01, COM-CMT-02, COM-INT-01)
	// 작성자 이름은 나중에 Member·Temple 원격 호출로 바뀐다. 그동안 DB 커넥션을 붙잡지 않도록 트랜잭션 전에 구한다.
	public CreateCommentResponse create(Requester requester, UUID postUuid, CreateCommentRequest request) {
		String authorName = authorNameProvider.getAuthorName(requester);
		Comment comment = commentWriter.create(postUuid, requester, authorName, request.content(),
			request.parentCommentUuid());
		return CreateCommentResponse.from(comment);
	}
}
