package com.nirvana.community.global.config;

import java.util.Arrays;
import java.util.List;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.nirvana.community.global.auth.CurrentRequester;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

// Swagger 화면 설정. 화면의 Authorize 버튼에서 Gateway 인증 헤더 2개를 넣고 API를 호출할 수 있게 한다.
@Configuration
public class OpenApiConfig {

	// global.auth.RequesterHeaders의 헤더 이름과 같아야 한다. 그 상수는 패키지 전용이라 여기 따로 둔다.
	private static final String USER_ID_HEADER = "X-User-Id";
	private static final String ACCOUNT_TYPE_HEADER = "X-Account-Type";

	@Bean
	public OpenAPI communityOpenApi() {
		return new OpenAPI()
			.info(new Info()
				.title("커뮤니티 서비스 API")
				.description("게시판, 게시글, 리뷰, 댓글, 좋아요, 신고 API. "
					+ "인증 헤더는 Gateway가 JWT를 검증한 뒤 넣어 준다. 로컬에서는 Authorize 버튼으로 직접 넣는다.")
				.version("v1"))
			.components(new Components()
				.addSecuritySchemes(USER_ID_HEADER, headerScheme(USER_ID_HEADER, "계정 UUID (36자 표준 형식)"))
				.addSecuritySchemes(ACCOUNT_TYPE_HEADER, headerScheme(ACCOUNT_TYPE_HEADER, "USER, OPERATOR, ADMIN 중 하나")))
			.addSecurityItem(authHeaders());
	}

	// 비회원도 부를 수 있는 API(@CurrentRequester(required = false))는 인증 헤더를 선택으로 표시한다.
	// 빈 요구사항({})이 함께 있으면 OpenAPI에서 "헤더 없이도 호출 가능"이라는 뜻이다.
	@Bean
	public OperationCustomizer optionalAuthForGuestApi() {
		return (operation, handlerMethod) -> {
			boolean guestAllowed = Arrays.stream(handlerMethod.getMethodParameters())
				.map(parameter -> parameter.getParameterAnnotation(CurrentRequester.class))
				.anyMatch(annotation -> annotation != null && !annotation.required());
			if (guestAllowed) {
				operation.setSecurity(List.of(new SecurityRequirement(), authHeaders()));
			}
			return operation;
		};
	}

	// 두 헤더를 함께 보내야 하므로 요구사항 하나에 둘 다 넣는다.
	private static SecurityRequirement authHeaders() {
		return new SecurityRequirement().addList(USER_ID_HEADER).addList(ACCOUNT_TYPE_HEADER);
	}

	private static SecurityScheme headerScheme(String headerName, String description) {
		return new SecurityScheme()
			.type(SecurityScheme.Type.APIKEY)
			.in(SecurityScheme.In.HEADER)
			.name(headerName)
			.description(description);
	}
}
