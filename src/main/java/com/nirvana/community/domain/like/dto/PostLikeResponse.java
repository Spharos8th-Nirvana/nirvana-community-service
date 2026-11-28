package com.nirvana.community.domain.like.dto;

// 좋아요·취소 결과. 반영된 뒤의 좋아요 수와 내 좋아요 상태다. (COM-LIK-05)
public record PostLikeResponse(
	long likeCount,
	boolean liked
) {
}
