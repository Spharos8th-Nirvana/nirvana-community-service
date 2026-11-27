package com.nirvana.community.domain.post.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import com.nirvana.community.domain.board.entity.BoardCategory;
import com.nirvana.community.domain.post.entity.PostStatus;
import com.nirvana.community.domain.post.stats.dto.PostCounts;

// 내가 쓴 글 목록 항목. 게시판 목록 항목(PostSummaryResponse)에 게시판 코드, 상태, 숨김 여부를 더했다. (COM-PST-25, 4.5)
// 게시판 목록 응답 형식은 그대로 두려고 따로 만들었다. 본문과 내부 id는 넣지 않는다(COM-PST-26, COM-NFR-01).
// 임시저장 글은 publishedAt이 null이다. 숨김 글은 hidden=true로 보인다(4.5).
public record MyPostSummaryResponse(
	UUID postUuid,
	String boardCode,
	PostStatus status,
	boolean hidden,
	String title,
	String authorName,
	@Nullable UUID categoryUuid,
	@Nullable String categoryName,
	@Nullable UUID thumbnailMediaUuid,
	@Nullable LocalDateTime publishedAt,
	long viewCount,
	long likeCount,
	long commentCount
) {

	public static MyPostSummaryResponse of(MyPostSummaryRow post, String boardCode, @Nullable BoardCategory category,
		PostCounts counts) {
		return new MyPostSummaryResponse(
			post.postUuid(),
			boardCode,
			post.status(),
			post.hidden(),
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
