package com.nirvana.community.domain.comment.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.nirvana.community.domain.comment.dto.CreateCommentRequest;
import com.nirvana.community.domain.comment.dto.CreateCommentResponse;
import com.nirvana.community.global.auth.Requester;
import com.nirvana.community.global.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

// 댓글 API의 Swagger 문서. 컨트롤러에는 요청 처리만 남기려고 문서 어노테이션을 여기에 둔다.
// 경로·요청 매핑(@PostMapping, @PathVariable 등)은 CommentController에 있다.
@Tag(name = "댓글", description = "댓글·답글 작성")
public interface CommentControllerDocs {

	@Operation(
		summary = "댓글·답글 작성",
		description = "로그인한 요청자가 댓글이 켜진 게시판의 공개 글에 쓴다. parentCommentUuid를 주면 그 댓글의 답글이 되고, 깊이 2인 댓글에는 답글을 달 수 없다. "
			+ "(COM-CMT-01, COM-CMT-02)")
	ResponseEntity<ApiResponse<CreateCommentResponse>> create(
		UUID postUuid,
		@Parameter(hidden = true) Requester requester,
		CreateCommentRequest request);
}
