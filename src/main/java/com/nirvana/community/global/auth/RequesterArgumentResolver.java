package com.nirvana.community.global.auth;

import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.nirvana.community.global.exception.BusinessException;
import com.nirvana.community.global.exception.ErrorCode;

// Gateway가 JWT를 검증한 뒤 넣어 준 헤더로 Requester를 만든다. (COM-BRD-02, AUTH-36)
public class RequesterArgumentResolver implements HandlerMethodArgumentResolver {

	static final String USER_ID_HEADER = "X-User-Id";
	static final String ACCOUNT_TYPE_HEADER = "X-Account-Type";

	// UUID.fromString은 "1-1-1-1-1"처럼 짧은 값도 받아 주므로 36자 표준 형식인지 먼저 확인한다.
	private static final Pattern UUID_PATTERN =
		Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(CurrentRequester.class)
			&& Requester.class.equals(parameter.getParameterType());
	}

	@Override
	public @Nullable Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
		NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
		String userId = webRequest.getHeader(USER_ID_HEADER);
		String accountType = webRequest.getHeader(ACCOUNT_TYPE_HEADER);
		if (isGuestAllowed(parameter) && userId == null && accountType == null) {
			return null;
		}
		return new Requester(parseAccountUuid(userId), parseAccountType(accountType));
	}

	private boolean isGuestAllowed(MethodParameter parameter) {
		return !parameter.getParameterAnnotation(CurrentRequester.class).required();
	}

	private UUID parseAccountUuid(String userId) {
		if (userId == null || !UUID_PATTERN.matcher(userId).matches()) {
			throw new BusinessException(ErrorCode.UNAUTHORIZED);
		}
		return UUID.fromString(userId);
	}

	private AccountType parseAccountType(String accountType) {
		return Arrays.stream(AccountType.values())
			.filter(type -> type.name().equals(accountType))
			.findFirst()
			.orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
	}
}
