package com.nirvana.community.domain.post.dto;

import java.util.UUID;

import com.nirvana.community.domain.post.entity.Post;
import com.nirvana.community.domain.post.entity.PostStatus;

// 고친 글의 식별자와 기본 정보. 내부 id는 주지 않는다. (COM-NFR-01)
// 본문·수정 일시가 필요하면 상세 조회로 읽는다.
public record UpdatePostResponse(
	UUID postUuid,
	String title,
	PostStatus status
) {

	public static UpdatePostResponse from(Post post) {
		return new UpdatePostResponse(post.getUuid(), post.getTitle(), post.getStatus());
	}
}
