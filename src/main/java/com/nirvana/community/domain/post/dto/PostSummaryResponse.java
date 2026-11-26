package com.nirvana.community.domain.post.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import com.nirvana.community.domain.board.entity.BoardCategory;
import com.nirvana.community.domain.post.stats.dto.PostCounts;

// 게시글 목록 항목. 본문은 넣지 않는다(COM-PST-26). 내부 id는 넣지 않는다(COM-NFR-01).
// publishedAt은 posts.scheduledAt(공개 시각) 값이다. (COM-PST-14, COM-PST-21)
// 카테고리를 쓰지 않는 글은 categoryUuid·categoryName이, 대표 이미지가 없는 글은 thumbnailMediaUuid가 null이다.
public record PostSummaryResponse(
	UUID postUuid,
	String title,
	String authorName,
	@Nullable UUID categoryUuid,
	@Nullable String categoryName,
	@Nullable UUID thumbnailMediaUuid,
	LocalDateTime publishedAt,
	long viewCount,
	long likeCount,
	long commentCount
) {

	public static PostSummaryResponse of(PostSummaryRow post, @Nullable BoardCategory category, PostCounts counts) {
		return new PostSummaryResponse(
			post.postUuid(),
			post.title(),
			post.authorName(),
			category == null ? null : category.getUuid(),
			category == null ? null : category.getName(),
			post.thumbnailMediaUuid(),
			post.publishedAt(),
			counts.viewCount(),
			counts.likeCount(),
			counts.commentCount());
	}
}
