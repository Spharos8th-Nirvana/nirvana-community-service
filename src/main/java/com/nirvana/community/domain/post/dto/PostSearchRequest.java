package com.nirvana.community.domain.post.dto;

import org.jspecify.annotations.Nullable;

import jakarta.validation.constraints.Size;

// 게시글 목록의 검색 조건(쿼리 파라미터). 지금은 제목 키워드 하나다. (이슈 #47)
// 컨트롤러가 Docs 인터페이스를 구현해서 @RequestParam에 바로 @Size를 붙이면 Bean Validation이
// 메서드 재정의 규칙 위반으로 거절한다. 그래서 다른 요청 DTO처럼 필드에 검증을 둔다.
public record PostSearchRequest(

	// 길이는 앞뒤 공백을 자르기 전 값으로 센다.
	@Size(max = 50, message = "검색어는 50자 이내로 입력해주세요.")
	@Nullable String keyword
) {

	// 앞뒤 공백을 자른 키워드. 보내지 않았거나 공백뿐이면 null이라 거르지 않는다.
	public @Nullable String keywordOrNull() {
		if (keyword == null || keyword.isBlank()) {
			return null;
		}
		return keyword.strip();
	}
}
