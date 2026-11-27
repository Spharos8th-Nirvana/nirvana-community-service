package com.nirvana.community.domain.post.controller;

import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springdoc.core.annotations.ParameterObject;
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
import com.nirvana.community.domain.post.dto.PostDetailResponse;
import com.nirvana.community.domain.post.dto.PostSummaryResponse;
import com.nirvana.community.domain.post.dto.UpdatePostRequest;
import com.nirvana.community.domain.post.dto.UpdatePostResponse;
import com.nirvana.community.domain.post.service.PostService;
import com.nirvana.community.global.auth.CurrentRequester;
import com.nirvana.community.global.auth.Requester;
import com.nirvana.community.global.response.ApiResponse;
import com.nirvana.community.global.response.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// 게시글 API. 작성자는 Gateway 인증 헤더의 요청자로 정한다. (COM-BRD-02)
@Tag(name = "게시글", description = "게시글 작성, 게시글 목록 조회, 게시글 상세 조회, 게시글 수정, 게시글 삭제")
@RestController
@RequiredArgsConstructor
public class PostController {

	private final PostService postService;

	@Operation(
		summary = "게시글 작성",
		description = "제목과 본문은 필수다. 본문 HTML은 허용한 서식 태그만 남기고 저장한다(img는 지금 지워진다). "
			+ "카테고리를 쓰는 게시판은 categoryUuid가 필수다. (COM-PST-01, COM-PST-02, COM-PST-04)")
	@PostMapping("/api/v1/boards/{boardCode}/posts")
	public ResponseEntity<ApiResponse<CreatePostResponse>> create(
		@PathVariable String boardCode,
		@Parameter(hidden = true) @CurrentRequester Requester requester,
		@Valid @RequestBody CreatePostRequest request) {
		CreatePostResponse response = postService.create(requester, boardCode, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
	}

	@Operation(
		summary = "게시글 목록 조회",
		description = "게시판의 공개 글을 최신순으로 조회한다. 로그인 없이 호출할 수 있다. 정렬은 고정이라 sort 값은 무시한다. "
			+ "(COM-PST-20, COM-PST-21)")
	@GetMapping("/api/v1/boards/{boardCode}/posts")
	public ApiResponse<PageResponse<PostSummaryResponse>> list(
		@PathVariable String boardCode,
		@Parameter(hidden = true) @CurrentRequester(required = false) @Nullable Requester requester,
		@RequestParam(required = false) @Nullable UUID categoryUuid,
		@ParameterObject Pageable pageable) {
		return ApiResponse.success(postService.listPosts(boardCode, requester, categoryUuid, pageable));
	}

	@Operation(
		summary = "게시글 상세 조회",
		description = "로그인 없이 호출할 수 있다. 임시저장·예약 글은 작성자 본인만 보고 그 외에는 404다. "
			+ "숨김·삭제 글은 200에 viewable=false이고 나머지 값은 null이다. (COM-PST-23, COM-PST-24)")
	@GetMapping("/api/v1/posts/{postUuid}")
	public ApiResponse<PostDetailResponse> get(
		@PathVariable UUID postUuid,
		@Parameter(hidden = true) @CurrentRequester(required = false) @Nullable Requester requester) {
		return ApiResponse.success(postService.getPost(postUuid, requester));
	}

	@Operation(
		summary = "게시글 수정",
		description = "작성자 본인만 수정한다(ADMIN도 403). 제목·본문·카테고리를 모두 보내며 규칙은 작성과 같다. 삭제된 글은 409. "
			+ "(COM-PST-08, COM-PST-15)")
	@PatchMapping("/api/v1/posts/{postUuid}")
	public ApiResponse<UpdatePostResponse> update(
		@PathVariable UUID postUuid,
		@Parameter(hidden = true) @CurrentRequester Requester requester,
		@Valid @RequestBody UpdatePostRequest request) {
		return ApiResponse.success(postService.update(requester, postUuid, request));
	}

	@Operation(
		summary = "게시글 삭제",
		description = "작성자 본인과 ADMIN만 삭제한다. 삭제 표시만 하며 다시 요청해도 204다. (COM-PST-09, COM-PST-15)")
	@DeleteMapping("/api/v1/posts/{postUuid}")
	public ResponseEntity<Void> delete(
		@PathVariable UUID postUuid,
		@Parameter(hidden = true) @CurrentRequester Requester requester) {
		postService.delete(requester, postUuid);
		return ResponseEntity.noContent().build();
	}
}
