package com.nirvana.community.domain.post.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.nirvana.community.domain.post.dto.CreatePostRequest;
import com.nirvana.community.domain.post.dto.CreatePostResponse;
import com.nirvana.community.domain.post.service.PostService;
import com.nirvana.community.global.auth.CurrentRequester;
import com.nirvana.community.global.auth.Requester;
import com.nirvana.community.global.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// 게시글 API. 작성자는 Gateway 인증 헤더의 요청자로 정한다. (COM-BRD-02)
@Tag(name = "게시글", description = "게시글 작성")
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
}
