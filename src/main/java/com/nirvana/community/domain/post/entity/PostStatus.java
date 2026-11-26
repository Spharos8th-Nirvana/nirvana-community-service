package com.nirvana.community.domain.post.entity;

// 게시글 발행 상태. 삭제와 숨김은 상태가 아니라 별도 표시(deleted, hidden)로 남긴다. (COM-PST-12)
public enum PostStatus {

	DRAFT,
	SCHEDULED,
	PUBLISHED
}
