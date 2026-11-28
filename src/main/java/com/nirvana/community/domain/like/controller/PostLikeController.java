package com.nirvana.community.domain.like.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nirvana.community.domain.like.dto.PostLikeResponse;
import com.nirvana.community.domain.like.service.PostLikeService;
import com.nirvana.community.global.auth.CurrentRequester;
import com.nirvana.community.global.auth.Requester;
import com.nirvana.community.global.auth.UserOnly;
import com.nirvana.community.global.response.ApiResponse;

import lombok.RequiredArgsConstructor;

// 게시글 좋아요 API. USER인지는 @UserOnly(인터셉터)가 Service 호출 전에 확인한다. (COM-LIK-01)
// 좋아요는 켜기(PUT)·끄기(DELETE)로 나눠 여러 번 불러도 결과가 같게 한다. (COM-LIK-04)
@UserOnly
@RestController
@RequiredArgsConstructor
public class PostLikeController implements PostLikeControllerDocs {

	private final PostLikeService postLikeService;

	@Override
	@PutMapping("/api/v1/posts/{postUuid}/like")
	public ApiResponse<PostLikeResponse> like(
		@PathVariable UUID postUuid,
		@CurrentRequester Requester requester) {
		return ApiResponse.success(postLikeService.like(requester, postUuid));
	}

	@Override
	@DeleteMapping("/api/v1/posts/{postUuid}/like")
	public ApiResponse<PostLikeResponse> unlike(
		@PathVariable UUID postUuid,
		@CurrentRequester Requester requester) {
		return ApiResponse.success(postLikeService.unlike(requester, postUuid));
	}
}
