package com.nirvana.community.domain.comment.controller;

import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import com.nirvana.community.domain.comment.dto.CommentResponse;
import com.nirvana.community.domain.comment.dto.CreateCommentRequest;
import com.nirvana.community.domain.comment.dto.CreateCommentResponse;
import com.nirvana.community.domain.comment.dto.UpdateCommentRequest;
import com.nirvana.community.domain.comment.dto.UpdateCommentResponse;
import com.nirvana.community.global.auth.Requester;
import com.nirvana.community.global.response.ApiResponse;
import com.nirvana.community.global.response.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

// 댓글 API의 Swagger 문서. 컨트롤러에는 요청 처리만 남기려고 문서 어노테이션을 여기에 둔다.
// 경로·요청 매핑(@PostMapping, @PathVariable 등)은 CommentController에 있다.
@Tag(name = "댓글", description = "댓글·답글 작성, 댓글 목록 조회, 댓글 수정, 댓글 삭제")
public interface CommentControllerDocs {

	@Operation(
		summary = "댓글·답글 작성",
		description = "로그인한 요청자가 댓글이 켜진 게시판의 공개 글에 쓴다. parentCommentUuid를 주면 그 댓글의 답글이 되고, 깊이 2인 댓글에는 답글을 달 수 없다. "
			+ "(COM-CMT-01, COM-CMT-02)")
	ResponseEntity<ApiResponse<CreateCommentResponse>> create(
		UUID postUuid,
		@Parameter(hidden = true) Requester requester,
		CreateCommentRequest request);

	@Operation(
		summary = "댓글 목록 조회",
		description = "글을 볼 수 있으면 로그인 없이 호출할 수 있다. 원댓글 기준으로 작성순 페이지를 나누고(sort 무시) 답글은 replies에 담으며, "
			+ "삭제·숨김 댓글은 남는 답글이 있을 때만 content 없이 자리만 나온다. (COM-CMT-03, COM-CMT-06, COM-CMT-09)")
	ApiResponse<PageResponse<CommentResponse>> list(
		UUID postUuid,
		@Parameter(hidden = true) @Nullable Requester requester,
		@ParameterObject Pageable pageable);

	@Operation(
		summary = "댓글 수정",
		description = "작성자 본인만 내용을 수정한다(ADMIN도 남의 댓글은 수정하지 못한다). 삭제된 댓글은 수정할 수 없다. (COM-CMT-04)")
	ApiResponse<UpdateCommentResponse> update(
		UUID commentUuid,
		@Parameter(hidden = true) Requester requester,
		UpdateCommentRequest request);

	@Operation(
		summary = "댓글 삭제",
		description = "작성자 본인과 ADMIN만 삭제한다. 삭제 표시만 하며 다시 요청해도 204다. (COM-CMT-05)")
	ResponseEntity<Void> delete(
		UUID commentUuid,
		@Parameter(hidden = true) Requester requester);
}
