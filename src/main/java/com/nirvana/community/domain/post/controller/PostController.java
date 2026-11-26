package com.nirvana.community.domain.post.controller;

import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nirvana.community.domain.post.dto.CreatePostRequest;
import com.nirvana.community.domain.post.dto.CreatePostResponse;
import com.nirvana.community.domain.post.dto.PostSummaryResponse;
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
@Tag(name = "게시글", description = "게시글 작성, 게시글 목록 조회")
@RestController
@RequiredArgsConstructor
public class PostController {

	private final PostService postService;

	@Operation(
		summary = "게시글 작성",
		description = "게시판에 글을 바로 공개 상태로 작성한다. 성공하면 201. 제목(200자 이내)과 본문은 필수다. "
			+ "본문 HTML은 허용한 서식 태그만 남기고 저장한다(img는 지금 모두 지운다). 정제한 뒤 글자가 없으면 400. "
			+ "카테고리를 쓰는 게시판은 그 게시판의 사용 중인 categoryUuid가 필수이고, 쓰지 않는 게시판은 보내면 400. "
			+ "REVIEW 게시판에는 쓸 수 없어 400. 게시판이 없거나 운영 중이 아니면 404. "
			+ "작성 권한이 없으면 403, 인증 헤더가 없거나 틀리면 401. (COM-PST-01, COM-PST-02, COM-PST-04, COM-PST-11, COM-BRD-01)")
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
		description = "게시판의 공개된 글을 페이지 단위로 조회한다. 임시저장·예약·숨김·삭제 글은 나오지 않는다. "
			+ "공개 시각(publishedAt) 최신순, 같으면 나중에 쓴 글 먼저다. sort 값은 받지 않고 무시한다. "
			+ "page는 0부터, size 기본 20·최대 100. 목록에는 본문이 없다. 조회수·좋아요 수·댓글 수는 집계 저장소가 "
			+ "응답하지 않으면 0이다. categoryUuid를 주면 그 카테고리 글만 보며, 그 게시판의 카테고리가 아니거나 없으면 400. "
			+ "로그인 없이 호출할 수 있고 게시판 열람 권한이 필요하다. 권한이 없으면 비회원 401, 로그인 사용자 403. "
			+ "게시판이 없거나 운영 중이 아니면 404. (COM-PST-20, COM-PST-21, COM-PST-26, COM-PST-14, COM-BRD-01, "
			+ "COM-NFR-02, COM-NFR-05, COM-NFR-08)")
	@GetMapping("/api/v1/boards/{boardCode}/posts")
	public ApiResponse<PageResponse<PostSummaryResponse>> list(
		@PathVariable String boardCode,
		@Parameter(hidden = true) @CurrentRequester(required = false) @Nullable Requester requester,
		@RequestParam(required = false) @Nullable UUID categoryUuid,
		@ParameterObject Pageable pageable) {
		return ApiResponse.success(postService.listPosts(boardCode, requester, categoryUuid, pageable));
	}
}
