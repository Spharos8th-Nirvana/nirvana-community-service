package com.nirvana.community.domain.post.controller;

import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import com.nirvana.community.domain.post.dto.CreatePostRequest;
import com.nirvana.community.domain.post.dto.CreatePostResponse;
import com.nirvana.community.domain.post.dto.MyPostSummaryResponse;
import com.nirvana.community.domain.post.dto.PostDetailResponse;
import com.nirvana.community.domain.post.dto.PostSummaryResponse;
import com.nirvana.community.domain.post.dto.UpdatePostRequest;
import com.nirvana.community.domain.post.dto.UpdatePostResponse;
import com.nirvana.community.domain.post.entity.PostStatus;
import com.nirvana.community.global.auth.Requester;
import com.nirvana.community.global.response.ApiResponse;
import com.nirvana.community.global.response.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

// 게시글 API의 Swagger 문서. 컨트롤러에는 요청 처리만 남기려고 문서 어노테이션을 여기에 둔다.
// 경로·요청 매핑(@GetMapping, @PathVariable 등)은 PostController에 있다.
@Tag(name = "게시글", description = "게시글 작성, 임시저장 글 공개, 게시글 목록 조회, 내가 쓴 글 목록 조회, 게시글 상세 조회, 게시글 수정, 게시글 삭제")
public interface PostControllerDocs {

	@Operation(
		summary = "게시글 작성",
		description = "status는 DRAFT(임시저장) 또는 PUBLISHED(바로 공개)이고 보내지 않으면 PUBLISHED다. 제목과 본문은 필수다. "
			+ "본문 HTML은 허용한 서식 태그만 남기고 저장한다(img는 지금 지워진다). "
			+ "카테고리를 쓰는 게시판은 categoryUuid가 필수다. "
			+ "(COM-PST-01, COM-PST-02, COM-PST-04, COM-PST-05)")
	ResponseEntity<ApiResponse<CreatePostResponse>> create(
		String boardCode,
		@Parameter(hidden = true) Requester requester,
		CreatePostRequest request);

	@Operation(
		summary = "게시글 목록 조회",
		description = "게시판의 공개 글을 최신순으로 조회한다. 로그인 없이 호출할 수 있다. 정렬은 고정이라 sort 값은 무시한다. "
			+ "(COM-PST-20, COM-PST-21)")
	ApiResponse<PageResponse<PostSummaryResponse>> list(
		String boardCode,
		@Parameter(hidden = true) @Nullable Requester requester,
		@Nullable UUID categoryUuid,
		@ParameterObject Pageable pageable);

	@Operation(
		summary = "내가 쓴 글 목록 조회",
		description = "내가 쓴 글을 최근에 쓴 순으로 조회한다. 임시저장·숨김 글을 포함하고 삭제한 글은 뺀다. status를 주면 그 상태의 글만 준다. "
			+ "(COM-PST-25)")
	ApiResponse<PageResponse<MyPostSummaryResponse>> listMine(
		@Parameter(hidden = true) Requester requester,
		@Nullable PostStatus status,
		@ParameterObject Pageable pageable);

	@Operation(
		summary = "게시글 상세 조회",
		description = "로그인 없이 호출할 수 있다. 임시저장·예약 글은 작성자 본인만 보고 그 외에는 404다. "
			+ "숨김·삭제 글은 200에 viewable=false이고 나머지 값은 null이다. (COM-PST-23, COM-PST-24)")
	ApiResponse<PostDetailResponse> get(
		UUID postUuid,
		@Parameter(hidden = true) @Nullable Requester requester);

	@Operation(
		summary = "게시글 수정",
		description = "작성자 본인만 수정한다(ADMIN도 403). 제목·본문·카테고리를 모두 보내며 규칙은 작성과 같다. 삭제된 글은 409. "
			+ "(COM-PST-08, COM-PST-15)")
	ApiResponse<UpdatePostResponse> update(
		UUID postUuid,
		@Parameter(hidden = true) Requester requester,
		UpdatePostRequest request);

	@Operation(
		summary = "임시저장 글 공개",
		description = "내 임시저장 글을 지금 공개한다. 작성자 본인만 할 수 있다. 이미 공개된 글과 삭제된 글은 409. "
			+ "(COM-PST-05, COM-PST-12)")
	ApiResponse<UpdatePostResponse> publish(
		UUID postUuid,
		@Parameter(hidden = true) Requester requester);

	@Operation(
		summary = "게시글 삭제",
		description = "작성자 본인과 ADMIN만 삭제한다. 삭제 표시만 하며 다시 요청해도 204다. (COM-PST-09, COM-PST-15)")
	ResponseEntity<Void> delete(
		UUID postUuid,
		@Parameter(hidden = true) Requester requester);
}
