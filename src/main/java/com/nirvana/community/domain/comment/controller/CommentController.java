package com.nirvana.community.domain.comment.controller;

import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.nirvana.community.domain.comment.dto.CommentResponse;
import com.nirvana.community.domain.comment.dto.CreateCommentRequest;
import com.nirvana.community.domain.comment.dto.CreateCommentResponse;
import com.nirvana.community.domain.comment.service.CommentService;
import com.nirvana.community.global.auth.CurrentRequester;
import com.nirvana.community.global.auth.Requester;
import com.nirvana.community.global.response.ApiResponse;
import com.nirvana.community.global.response.PageResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// 댓글 API. 작성자는 Gateway 인증 헤더의 요청자로 정한다. (COM-BRD-02)
@RestController
@RequiredArgsConstructor
public class CommentController implements CommentControllerDocs {

	private final CommentService commentService;

	@Override
	@PostMapping("/api/v1/posts/{postUuid}/comments")
	public ResponseEntity<ApiResponse<CreateCommentResponse>> create(
		@PathVariable UUID postUuid,
		@CurrentRequester Requester requester,
		@Valid @RequestBody CreateCommentRequest request) {
		CreateCommentResponse response = commentService.create(requester, postUuid, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
	}

	@Override
	@GetMapping("/api/v1/posts/{postUuid}/comments")
	public ApiResponse<PageResponse<CommentResponse>> list(
		@PathVariable UUID postUuid,
		@CurrentRequester(required = false) @Nullable Requester requester,
		Pageable pageable) {
		return ApiResponse.success(commentService.list(postUuid, requester, pageable));
	}
}
