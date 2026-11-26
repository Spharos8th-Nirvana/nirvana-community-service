package com.nirvana.community.global.auth;

import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

// Gateway가 JWT를 검증한 뒤 넣어 준 헤더로 Requester를 만든다. (COM-BRD-02, AUTH-36)
public class RequesterArgumentResolver implements HandlerMethodArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(CurrentRequester.class)
			&& Requester.class.equals(parameter.getParameterType());
	}

	@Override
	public @Nullable Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
		NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
		String userId = webRequest.getHeader(RequesterHeaders.USER_ID);
		String accountType = webRequest.getHeader(RequesterHeaders.ACCOUNT_TYPE);
		if (isGuestAllowed(parameter) && userId == null && accountType == null) {
			return null;
		}
		return RequesterHeaders.parse(userId, accountType);
	}

	private boolean isGuestAllowed(MethodParameter parameter) {
		return !parameter.getParameterAnnotation(CurrentRequester.class).required();
	}
}
