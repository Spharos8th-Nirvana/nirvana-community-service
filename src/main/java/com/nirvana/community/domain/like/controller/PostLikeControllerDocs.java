package com.nirvana.community.domain.like.controller;

import java.util.UUID;

import com.nirvana.community.domain.like.dto.PostLikeResponse;
import com.nirvana.community.global.auth.Requester;
import com.nirvana.community.global.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

// 게시글 좋아요 API의 Swagger 문서. 컨트롤러에는 요청 처리만 남기려고 문서 어노테이션을 여기에 둔다.
// 경로·요청 매핑(@PutMapping, @PathVariable 등)은 PostLikeController에 있다.
@Tag(name = "게시글 좋아요", description = "게시글 좋아요, 게시글 좋아요 취소")
public interface PostLikeControllerDocs {

	@Operation(
		summary = "게시글 좋아요",
		description = "USER만 호출할 수 있다. 이미 누른 글에 다시 눌러도 좋아요 수는 그대로이고, 응답은 반영된 좋아요 수와 liked=true다. "
			+ "(COM-LIK-01, COM-LIK-04, COM-LIK-05, COM-LIK-12)")
	ApiResponse<PostLikeResponse> like(
		UUID postUuid,
		@Parameter(hidden = true) Requester requester);

	@Operation(
		summary = "게시글 좋아요 취소",
		description = "USER만 호출할 수 있다. 누르지 않았거나 이미 취소한 글에 다시 취소해도 좋아요 수는 그대로이고, 응답은 반영된 좋아요 수와 liked=false다. "
			+ "누를 수 없는 글(숨김·삭제 등)은 취소도 할 수 없다. (COM-LIK-02, COM-LIK-04, COM-LIK-05)")
	ApiResponse<PostLikeResponse> unlike(
		UUID postUuid,
		@Parameter(hidden = true) Requester requester);
}
