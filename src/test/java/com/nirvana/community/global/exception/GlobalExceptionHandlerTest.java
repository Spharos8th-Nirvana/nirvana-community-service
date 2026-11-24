package com.nirvana.community.global.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nirvana.community.global.response.ApiResponse;

class GlobalExceptionHandlerTest {

	private static final String INTERNAL_DETAIL = "SELECT password FROM members WHERE id = 1";

	private final MockMvc mockMvc = MockMvcBuilders
		.standaloneSetup(new TestController())
		.setControllerAdvice(new GlobalExceptionHandler())
		.build();

	@Test
	void 성공_응답은_success_true_data_error_null이다() throws Exception {
		mockMvc.perform(get("/test/success"))
			.andExpect(status().isOk())
			.andExpect(content().json("""
				{ "success": true, "data": "ok", "error": null }
				""", JsonCompareMode.STRICT));
	}

	@Test
	void BusinessException은_ErrorCode의_상태코드와_실패_응답을_준다() throws Exception {
		ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;

		mockMvc.perform(get("/test/business"))
			.andExpect(status().is(errorCode.getStatus().value()))
			.andExpect(content().json("""
				{
				  "success": false,
				  "data": null,
				  "error": { "code": "%s", "message": "%s" }
				}
				""".formatted(errorCode.getCode(), errorCode.getMessage()), JsonCompareMode.STRICT));
	}

	@Test
	void 예상하지_못한_예외는_500과_일반화된_메시지를_주고_내부_정보를_노출하지_않는다() throws Exception {
		String body = mockMvc.perform(get("/test/unexpected"))
			.andExpect(status().isInternalServerError())
			.andExpect(content().json("""
				{
				  "success": false,
				  "data": null,
				  "error": { "code": "INTERNAL_SERVER_ERROR", "message": "서버 내부 오류가 발생했습니다." }
				}
				""", JsonCompareMode.STRICT))
			.andReturn()
			.getResponse()
			.getContentAsString();

		assertThat(body).doesNotContain(INTERNAL_DETAIL, "IllegalStateException", "at com.nirvana");
	}

	@RestController
	static class TestController {

		@GetMapping("/test/success")
		ApiResponse<String> success() {
			return ApiResponse.success("ok");
		}

		@GetMapping("/test/business")
		ApiResponse<Void> business() {
			throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
		}

		@GetMapping("/test/unexpected")
		ApiResponse<Void> unexpected() {
			throw new IllegalStateException(INTERNAL_DETAIL);
		}
	}
}
