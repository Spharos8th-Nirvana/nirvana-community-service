package com.nirvana.community.domain.post.dto;

import org.jspecify.annotations.Nullable;

import com.nirvana.community.domain.board.entity.BoardCategory;
import com.nirvana.community.domain.post.entity.Post;

// 게시글 상세를 만들 때 MySQL에서 읽은 것을 한데 담는다. PostQueryService가 만들고 PostService가 응답으로 바꾼다.
// 카테고리를 쓰지 않는 글은 category가 null이다.
public record PostDetailRow(
	Post post,
	String boardCode,
	@Nullable BoardCategory category
) {
}
