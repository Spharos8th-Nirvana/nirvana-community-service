package com.nirvana.community.domain.post.stats.dto;

import com.nirvana.community.domain.post.stats.document.PostStats;

// 목록·상세에 내려 줄 게시글 집계 수. (COM-PST-21, COM-PST-23)
public record PostCounts(long viewCount, long likeCount, long commentCount) {

	// 집계 문서가 아직 없는 글(새 글 등)은 세 수가 모두 0이다.
	public static final PostCounts ZERO = new PostCounts(0L, 0L, 0L);

	public static PostCounts from(PostStats postStats) {
		return new PostCounts(postStats.viewCount(), postStats.likeCount(), postStats.commentCount());
	}
}
