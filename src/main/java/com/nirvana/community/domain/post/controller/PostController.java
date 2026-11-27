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

	@Operation(
		summary = "게시글 상세 조회",
		description = "글 하나의 제목, 본문(정제된 HTML), 작성자, 게시판·카테고리, 일시, 조회수·좋아요 수·댓글 수, 내 글인지(mine)를 준다. "
			+ "공개 글은 누구나 본다. 임시저장·예약 글은 작성자 본인만 보고, 그 외(ADMIN 포함)에는 없는 글로 404. "
			+ "숨김 글은 ADMIN과 작성자 본인만 본문을 보고, 삭제 글은 아무도 못 본다. 볼 수 없는 글은 200에 viewable=false이고 "
			+ "postUuid 외 항목은 모두 null이다. likedByMe·pinned는 기능이 아직 없어 항상 false다. 조회수는 올리지 않는다. "
			+ "집계 저장소가 응답하지 않으면 수는 0이다. 로그인 없이 호출할 수 있고 글이 속한 게시판의 열람 권한이 필요하다. "
			+ "권한이 없으면 비회원 401, 로그인 사용자 403. 글이 없으면 404(POST_NOT_FOUND), 게시판이 운영 중이 아니면 "
			+ "404(BOARD_NOT_FOUND), postUuid 형식이 틀리면 400. (COM-PST-23, COM-PST-24, COM-PST-09, COM-PST-12, "
			+ "COM-BRD-01, COM-NFR-01, COM-NFR-05)")
	@GetMapping("/api/v1/posts/{postUuid}")
	public ApiResponse<PostDetailResponse> get(
		@PathVariable UUID postUuid,
		@Parameter(hidden = true) @CurrentRequester(required = false) @Nullable Requester requester) {
		return ApiResponse.success(postService.getPost(postUuid, requester));
	}

	@Operation(
		summary = "게시글 수정",
		description = "내 글의 제목, 본문, 카테고리를 바꾼다. 세 값을 모두 보내며 규칙은 작성과 같다: 제목(200자 이내)과 본문은 필수, "
			+ "본문 HTML은 허용한 서식 태그만 남기고 정제한 뒤 글자가 없으면 400, 카테고리를 쓰는 게시판은 사용 중인 categoryUuid가 "
			+ "필수이고 쓰지 않는 게시판은 보내면 400. 수정한 사람의 구분과 수정 시각을 남긴다. "
			+ "작성자 본인만 수정할 수 있고 다른 사람·ADMIN은 403. 삭제된 글은 409(POST_ALREADY_DELETED). "
			+ "글이 없거나 남의 임시저장·예약 글이면 404(POST_NOT_FOUND), 게시판이 운영 중이 아니면 404(BOARD_NOT_FOUND). "
			+ "게시판 작성 권한이 없으면 403, 인증 헤더가 없거나 틀리면 401. (COM-PST-08, COM-PST-15, COM-PST-02, COM-PST-04, "
			+ "COM-BRD-04, COM-BRD-01)")
	@PatchMapping("/api/v1/posts/{postUuid}")
	public ApiResponse<UpdatePostResponse> update(
		@PathVariable UUID postUuid,
		@Parameter(hidden = true) @CurrentRequester Requester requester,
		@Valid @RequestBody UpdatePostRequest request) {
		return ApiResponse.success(postService.update(requester, postUuid, request));
	}

	@Operation(
		summary = "게시글 삭제",
		description = "글을 지우지 않고 삭제 표시만 한다. 글의 상태(공개·임시저장·예약)는 바꾸지 않는다. 성공하면 204. "
			+ "이미 삭제된 글을 다시 삭제해도 204다. 삭제한 글은 목록에 나오지 않고 상세는 '볼 수 없는 글'이 된다. "
			+ "작성자 본인과 ADMIN만 삭제할 수 있고 그 외는 403. 글이 없거나 남의 임시저장·예약 글이면 ADMIN도 404(POST_NOT_FOUND). "
			+ "인증 헤더가 없거나 틀리면 401. (COM-PST-09, COM-PST-12, COM-PST-15, COM-BRD-04)")
	@DeleteMapping("/api/v1/posts/{postUuid}")
	public ResponseEntity<Void> delete(
		@PathVariable UUID postUuid,
		@Parameter(hidden = true) @CurrentRequester Requester requester) {
		postService.delete(requester, postUuid);
		return ResponseEntity.noContent().build();
	}
}
