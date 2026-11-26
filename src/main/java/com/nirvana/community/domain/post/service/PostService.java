package com.nirvana.community.domain.post.service;

import org.springframework.stereotype.Service;

import com.nirvana.community.domain.post.dto.CreatePostRequest;
import com.nirvana.community.domain.post.dto.CreatePostResponse;
import com.nirvana.community.domain.post.entity.Post;
import com.nirvana.community.global.auth.Requester;

import lombok.RequiredArgsConstructor;

// 이 클래스에는 일부러 @Transactional을 붙이지 않는다. 트랜잭션은 PostWriter.create 안에서만 열린다.
@Service
@RequiredArgsConstructor
public class PostService {

	private final PostContentSanitizer postContentSanitizer;
	private final AuthorNameProvider authorNameProvider;
	private final PostWriter postWriter;

	// 본문 정제 → 작성자 이름 조회 → (트랜잭션) 확인과 저장. (COM-PST-01, COM-PST-04)
	// 작성자 이름은 나중에 Member·Temple 원격 호출로 바뀐다. 그동안 DB 커넥션을 붙잡지 않도록 트랜잭션 전에 구한다.
	public CreatePostResponse create(Requester requester, String boardCode, CreatePostRequest request) {
		String content = postContentSanitizer.sanitize(request.content());
		String authorName = authorNameProvider.getAuthorName(requester);
		Post post = postWriter.create(boardCode, requester, authorName, request.title(), content,
			request.categoryUuid());
		return CreatePostResponse.from(post);
	}
}
