package com.nirvana.community.domain.post.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

// 게시글 목록 쿼리가 읽는 컬럼만 담는다. 본문(MEDIUMTEXT)은 목록에 쓰지 않아 넣지 않는다. (COM-PST-26)
// categoryId는 카테고리 이름을 한 번에 찾을 때만 쓰고 응답에는 내보내지 않는다. (COM-NFR-01)
public record PostSummaryRow(
	UUID postUuid,
	String title,
	String authorName,
	@Nullable Long categoryId,
	@Nullable UUID thumbnailMediaUuid,
	LocalDateTime publishedAt
) {
}
