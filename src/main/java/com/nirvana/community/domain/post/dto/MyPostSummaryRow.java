package com.nirvana.community.domain.post.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import com.nirvana.community.domain.post.entity.PostStatus;

// 내가 쓴 글 목록 쿼리가 읽는 컬럼만 담는다. 본문은 넣지 않는다. (COM-PST-25, COM-PST-26)
// typeId·categoryId는 게시판 코드·카테고리 이름을 한 번에 찾을 때만 쓰고 응답에는 내보내지 않는다. (COM-NFR-01)
// 임시저장 글은 공개 시각이 없어 publishedAt이 null이다.
public record MyPostSummaryRow(
	UUID postUuid,
	String title,
	String authorName,
	Long typeId,
	@Nullable Long categoryId,
	@Nullable UUID thumbnailMediaUuid,
	PostStatus status,
	boolean hidden,
	@Nullable LocalDateTime publishedAt
) {
}
