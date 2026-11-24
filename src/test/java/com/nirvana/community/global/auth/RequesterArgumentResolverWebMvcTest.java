package com.nirvana.community.global.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.micrometer.observation.autoconfigure.ObservationAutoConfiguration;
import org.springframework.boot.micrometer.tracing.autoconfigure.MicrometerTracingAutoConfiguration;
import org.springframework.boot.micrometer.tracing.brave.autoconfigure.BraveAutoConfiguration;
import org.springframework.boot.webmvc.autoconfigure.WebMvcObservationAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nirvana.community.global.response.ApiResponse;

// 실제 앱과 같은 MVC 설정(RequesterWebMvcConfig, GlobalExceptionHandler)으로 인증 헤더 처리를 확인한다. DB 연결이 필요 없다.
// 401 응답의 traceId까지 확인하려고 Trace ID 전달에 필요한 자동 설정만 가져온다.
@WebMvcTest(controllers = RequesterArgumentResolverWebMvcTest.TestController.class)
@Import(RequesterArgumentResolverWebMvcTest.TestController.class)
@ImportAutoConfiguration({
	ObservationAutoConfiguration.class,
	MicrometerTracingAutoConfiguration.class,
	BraveAutoConfiguration.class,
	WebMvcObservationAutoConfiguration.class
})
class RequesterArgumentResolverWebMvcTest {

	private static final String USER_ID_HEADER = "X-User-Id";
	private static final String ACCOUNT_TYPE_HEADER = "X-Account-Type";
	private static final String ACCOUNT_UUID = "3f2504e0-4f89-41d3-9a0c-0305e82c3301";
	private static final String REQUIRED_URL = "/test/requester";
	private static final String OPTIONAL_URL = "/test/requester/optional";
	private static final String TRACE_ID = "4bf92f3577b34da6a3ce929d0e0e4736";
	private static final String TRACEPARENT = "00-" + TRACE_ID + "-00f067aa0ba902b7-01";

	@Autowired
	private MockMvc mockMvc;

	@ParameterizedTest
	@ValueSource(strings = {"USER", "OPERATOR", "ADMIN"})
	void 정상_헤더면_요청자의_UUID와_역할을_파라미터로_받는다(String accountType) throws Exception {
		mockMvc.perform(get(REQUIRED_URL)
				.header(USER_ID_HEADER, ACCOUNT_UUID)
				.header(ACCOUNT_TYPE_HEADER, accountType))
			.andExpect(status().isOk())
			.andExpect(content().json("""
				{
				  "success": true,
				  "data": { "accountUuid": "%s", "accountType": "%s" },
				  "error": null
				}
				""".formatted(ACCOUNT_UUID, accountType), JsonCompareMode.STRICT));
	}

	@Test
	void 대문자_UUID도_같은_UUID로_받는다() throws Exception {
		mockMvc.perform(get(REQUIRED_URL)
				.header(USER_ID_HEADER, ACCOUNT_UUID.toUpperCase())
				.header(ACCOUNT_TYPE_HEADER, "USER"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.accountUuid").value(ACCOUNT_UUID));
	}

	@Test
	void 인증_헤더가_없으면_401_UNAUTHORIZED를_공통_오류_형식으로_준다() throws Exception {
		mockMvc.perform(get(REQUIRED_URL).header("traceparent", TRACEPARENT))
			.andExpect(status().isUnauthorized())
			.andExpect(content().json("""
				{
				  "success": false,
				  "data": null,
				  "error": { "code": "UNAUTHORIZED", "message": "로그인이 필요합니다.", "traceId": "%s" }
				}
				""".formatted(TRACE_ID), JsonCompareMode.STRICT));
	}

	@Test
	void X_User_Id만_있으면_401이다() throws Exception {
		expectUnauthorized(mockMvc.perform(get(REQUIRED_URL).header(USER_ID_HEADER, ACCOUNT_UUID)));
	}

	@Test
	void X_Account_Type만_있으면_401이다() throws Exception {
		expectUnauthorized(mockMvc.perform(get(REQUIRED_URL).header(ACCOUNT_TYPE_HEADER, "USER")));
	}

	@ParameterizedTest
	@ValueSource(strings = {"not-a-uuid", "1-1-1-1-1", "3f2504e04f8941d39a0c0305e82c3301", ""})
	void UUID_형식이_틀리면_401이다(String userId) throws Exception {
		expectUnauthorized(mockMvc.perform(get(REQUIRED_URL)
			.header(USER_ID_HEADER, userId)
			.header(ACCOUNT_TYPE_HEADER, "USER")));
	}

	@ParameterizedTest
	@ValueSource(strings = {"GUEST", "OWNER", "SUPER_ADMIN", "user", ""})
	void 모르는_역할_값이면_401이다(String accountType) throws Exception {
		expectUnauthorized(mockMvc.perform(get(REQUIRED_URL)
			.header(USER_ID_HEADER, ACCOUNT_UUID)
			.header(ACCOUNT_TYPE_HEADER, accountType)));
	}

	@Test
	void 로그인_선택에서_인증_헤더가_둘_다_없으면_비회원_null로_받는다() throws Exception {
		mockMvc.perform(get(OPTIONAL_URL))
			.andExpect(status().isOk())
			.andExpect(content().json("""
				{ "success": true, "data": "GUEST", "error": null }
				""", JsonCompareMode.STRICT));
	}

	@Test
	void 로그인_선택에서_정상_헤더면_요청자_정보를_받는다() throws Exception {
		mockMvc.perform(get(OPTIONAL_URL)
				.header(USER_ID_HEADER, ACCOUNT_UUID)
				.header(ACCOUNT_TYPE_HEADER, "OPERATOR"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data").value("OPERATOR"));
	}

	@Test
	void 로그인_선택에서_UUID가_잘못되면_401이다() throws Exception {
		expectUnauthorized(mockMvc.perform(get(OPTIONAL_URL)
			.header(USER_ID_HEADER, "not-a-uuid")
			.header(ACCOUNT_TYPE_HEADER, "USER")));
	}

	@Test
	void 로그인_선택에서_역할_값이_잘못되면_401이다() throws Exception {
		expectUnauthorized(mockMvc.perform(get(OPTIONAL_URL)
			.header(USER_ID_HEADER, ACCOUNT_UUID)
			.header(ACCOUNT_TYPE_HEADER, "GUEST")));
	}

	@Test
	void 로그인_선택에서_헤더가_하나만_있으면_401이다() throws Exception {
		expectUnauthorized(mockMvc.perform(get(OPTIONAL_URL).header(USER_ID_HEADER, ACCOUNT_UUID)));
		expectUnauthorized(mockMvc.perform(get(OPTIONAL_URL).header(ACCOUNT_TYPE_HEADER, "USER")));
	}

	private void expectUnauthorized(ResultActions result) throws Exception {
		result
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"))
			.andExpect(jsonPath("$.error.message").value("로그인이 필요합니다."));
	}

	@RestController
	static class TestController {

		@GetMapping(REQUIRED_URL)
		ApiResponse<Requester> requester(@CurrentRequester Requester requester) {
			return ApiResponse.success(requester);
		}

		@GetMapping(OPTIONAL_URL)
		ApiResponse<String> optionalRequester(@CurrentRequester(required = false) Requester requester) {
			if (requester == null) {
				return ApiResponse.success("GUEST");
			}
			return ApiResponse.success(requester.accountType().name());
		}
	}
}
