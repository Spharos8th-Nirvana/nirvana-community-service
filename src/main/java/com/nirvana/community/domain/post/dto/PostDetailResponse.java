package com.nirvana.community.domain.post.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import com.nirvana.community.domain.post.entity.Post;
import com.nirvana.community.domain.post.stats.dto.PostCounts;
import com.nirvana.community.global.auth.AccountType;
import com.nirvana.community.global.auth.Requester;

// 게시글 상세. 내부 id는 넣지 않는다(COM-NFR-01). (COM-PST-23)
// viewable이 false면 "볼 수 없는 글"(숨김·삭제)이다. postUuid와 viewable만 채우고 나머지는 모두 null이다.
// 숨김·삭제된 글의 내용이 새지 않게 제목·본문뿐 아니라 게시판·일시·집계 수까지 가린다. (COM-PST-24, 4.5)
// 본문(content)은 저장할 때 이미 정제한 HTML이라 그대로 준다. (COM-PST-04)
// publishedAt은 posts.scheduledAt(공개 시각)이다. 임시저장 글은 null이다. (COM-PST-14)
public record PostDetailResponse(
	UUID postUuid,
	boolean viewable,
	@Nullable String title,
	@Nullable String content,
	@Nullable String authorName,
	@Nullable AccountType authorType,
	@Nullable String boardCode,
	@Nullable UUID categoryUuid,
	@Nullable String categoryName,
	@Nullable UUID thumbnailMediaUuid,
	@Nullable LocalDateTime publishedAt,
	@Nullable LocalDateTime createdAt,
	@Nullable LocalDateTime updatedAt,
	@Nullable Long viewCount,
	@Nullable Long likeCount,
	@Nullable Long commentCount,
	@Nullable Boolean mine,
	@Nullable Boolean likedByMe,
	@Nullable Boolean pinned
) {

	// 좋아요(likedByMe)·상단 고정(pinned) 기능이 아직 없어 둘 다 항상 false다. 기능이 생기면 실제 값으로 바꾼다.
	public static PostDetailResponse of(PostDetailRow row, PostCounts counts, @Nullable Requester requester) {
		Post post = row.post();
		return new PostDetailResponse(
			post.getUuid(),
			true,
			post.getTitle(),
			post.getContent(),
			post.getAuthorName(),
			post.getAuthorType(),
			row.boardCode(),
			row.category() == null ? null : row.category().getUuid(),
			row.category() == null ? null : row.category().getName(),
			post.getThumbnailMediaUuid(),
			post.getScheduledAt(),
			post.getCreatedAt(),
			post.getUpdatedAt(),
			counts.viewCount(),
			counts.likeCount(),
			counts.commentCount(),
			post.isWrittenBy(requester),
			false,
			false);
	}

	// "볼 수 없는 글". 숨김인지 삭제인지는 요구사항에 구분해 알려 주라는 내용이 없어 넣지 않는다.
	public static PostDetailResponse unviewable(UUID postUuid) {
		return new PostDetailResponse(postUuid, false, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null);
	}
}
