package com.nirvana.community.domain.post.controller;

import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nirvana.community.domain.post.dto.CreatePostRequest;
import com.nirvana.community.domain.post.dto.CreatePostResponse;
import com.nirvana.community.domain.post.dto.MyPostSummaryResponse;
import com.nirvana.community.domain.post.dto.PostDetailResponse;
import com.nirvana.community.domain.post.dto.PostSummaryResponse;
import com.nirvana.community.domain.post.dto.UpdatePostRequest;
import com.nirvana.community.domain.post.dto.UpdatePostResponse;
import com.nirvana.community.domain.post.entity.PostStatus;
import com.nirvana.community.domain.post.service.PostService;
import com.nirvana.community.global.auth.CurrentRequester;
import com.nirvana.community.global.auth.Requester;
import com.nirvana.community.global.response.ApiResponse;
import com.nirvana.community.global.response.PageResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// 게시글 API. 작성자는 Gateway 인증 헤더의 요청자로 정한다. (COM-BRD-02)
@RestController
@RequiredArgsConstructor
public class PostController implements PostControllerDocs {

	private final PostService postService;

	@Override
	@PostMapping("/api/v1/boards/{boardCode}/posts")
	public ResponseEntity<ApiResponse<CreatePostResponse>> create(
		@PathVariable String boardCode,
		@CurrentRequester Requester requester,
		@Valid @RequestBody CreatePostRequest request) {
		CreatePostResponse response = postService.create(requester, boardCode, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
	}

	@Override
	@GetMapping("/api/v1/boards/{boardCode}/posts")
	public ApiResponse<PageResponse<PostSummaryResponse>> list(
		@PathVariable String boardCode,
		@CurrentRequester(required = false) @Nullable Requester requester,
		@RequestParam(required = false) @Nullable UUID categoryUuid,
		Pageable pageable) {
		return ApiResponse.success(postService.listPosts(boardCode, requester, categoryUuid, pageable));
	}

	@Override
	@GetMapping("/api/v1/posts/me")
	public ApiResponse<PageResponse<MyPostSummaryResponse>> listMine(
		@CurrentRequester Requester requester,
		@RequestParam(required = false) @Nullable PostStatus status,
		Pageable pageable) {
		return ApiResponse.success(postService.listMyPosts(requester, status, pageable));
	}

	@Override
	@GetMapping("/api/v1/posts/{postUuid}")
	public ApiResponse<PostDetailResponse> get(
		@PathVariable UUID postUuid,
		@CurrentRequester(required = false) @Nullable Requester requester) {
		return ApiResponse.success(postService.getPost(postUuid, requester));
	}

	@Override
	@PatchMapping("/api/v1/posts/{postUuid}")
	public ApiResponse<UpdatePostResponse> update(
		@PathVariable UUID postUuid,
		@CurrentRequester Requester requester,
		@Valid @RequestBody UpdatePostRequest request) {
		return ApiResponse.success(postService.update(requester, postUuid, request));
	}

	@Override
	@PostMapping("/api/v1/posts/{postUuid}/publish")
	public ApiResponse<UpdatePostResponse> publish(
		@PathVariable UUID postUuid,
		@CurrentRequester Requester requester) {
		return ApiResponse.success(postService.publish(requester, postUuid));
	}

	@Override
	@DeleteMapping("/api/v1/posts/{postUuid}")
	public ResponseEntity<Void> delete(
		@PathVariable UUID postUuid,
		@CurrentRequester Requester requester) {
		postService.delete(requester, postUuid);
		return ResponseEntity.noContent().build();
	}
}
