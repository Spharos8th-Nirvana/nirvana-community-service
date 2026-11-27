package com.nirvana.community.domain.post.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import com.nirvana.community.domain.post.entity.Post;
import com.nirvana.community.domain.post.entity.PostStatus;

// 만든 글의 식별자와 기본 정보. 내부 id는 주지 않는다. (COM-NFR-01)
// publishedAt은 posts.scheduledAt(공개 시각) 값이다. 임시저장 글은 아직 공개되지 않아 null이다. (COM-PST-14, COM-PST-05)
public record CreatePostResponse(
	UUID postUuid,
	String title,
	String authorName,
	PostStatus status,
	@Nullable LocalDateTime publishedAt
) {

	public static CreatePostResponse from(Post post) {
		return new CreatePostResponse(
			post.getUuid(), post.getTitle(), post.getAuthorName(), post.getStatus(), post.getScheduledAt());
	}
}
