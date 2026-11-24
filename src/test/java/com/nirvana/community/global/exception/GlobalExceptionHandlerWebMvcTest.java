package com.nirvana.community.global.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.micrometer.observation.autoconfigure.ObservationAutoConfiguration;
import org.springframework.boot.micrometer.tracing.autoconfigure.MicrometerTracingAutoConfiguration;
import org.springframework.boot.micrometer.tracing.brave.autoconfigure.BraveAutoConfiguration;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.autoconfigure.WebMvcObservationAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nirvana.community.global.response.ApiResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 실제 앱과 같은 MVC 설정(404 처리, 메시지 변환, 검증)으로 오류 응답을 확인한다. MVC 슬라이스라 DB 연결이 필요 없다.
// @WebMvcTest는 트레이싱 자동 설정을 띄우지 않으므로, Trace ID 생성·전달에 필요한 자동 설정만 직접 가져온다.
@WebMvcTest(controllers = GlobalExceptionHandlerWebMvcTest.TestController.class)
@Import(GlobalExceptionHandlerWebMvcTest.TestController.class)
@ImportAutoConfiguration({
	ObservationAutoConfiguration.class,
	MicrometerTracingAutoConfiguration.class,
	BraveAutoConfiguration.class,
	WebMvcObservationAutoConfiguration.class
})
@ExtendWith(OutputCaptureExtension.class)
class GlobalExceptionHandlerWebMvcTest {

	private static final String TRACE_ID = "4bf92f3577b34da6a3ce929d0e0e4736";
	private static final String TRACEPARENT = "00-" + TRACE_ID + "-00f067aa0ba902b7-01";
	private static final String TRACE_ID_PATTERN = "^[0-9a-f]{32}$";
	private static final String INTERNAL_DETAIL = "SELECT password FROM members WHERE id = 1";

	@Autowired
	private MockMvc mockMvc;

	@Test
	void traceparent로_들어온_Trace_ID가_오류_응답의_traceId에_그대로_나온다() throws Exception {
		mockMvc.perform(get("/test/unknown").header("traceparent", TRACEPARENT))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error.traceId").value(TRACE_ID));
	}

	@Test
	void traceparent가_없으면_새_Trace_ID를_만들어_오류_응답에_넣는다() throws Exception {
		mockMvc.perform(get("/test/unknown"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error.traceId").value(matchesPattern(TRACE_ID_PATTERN)));
	}

	@Test
	void 오류_응답의_traceId는_같은_요청의_서버_로그_Trace_ID와_같다(CapturedOutput output) throws Exception {
		String logTraceId = "0af7651916cd43dd8448eb211c80319c";
		mockMvc.perform(get("/test/unknown").header("traceparent", "00-" + logTraceId + "-b7ad6b7169203331-01"))
			.andExpect(jsonPath("$.error.traceId").value(logTraceId));

		// Spring Boot 기본 로그 패턴은 [traceId-spanId] 형식으로 Trace ID를 찍는다.
		assertThat(output.getAll())
			.containsPattern("\\[" + logTraceId + "-[0-9a-f]{16}\\].*Invalid request: status=404");
	}

	@Test
	void 성공_응답에는_traceId가_없다() throws Exception {
		mockMvc.perform(get("/test/success").header("traceparent", TRACEPARENT))
			.andExpect(status().isOk())
			.andExpect(content().json("""
				{ "success": true, "data": "ok", "error": null }
				""", JsonCompareMode.STRICT));
	}

	@Test
	void 요청_DTO_검증에_실패하면_400_VALIDATION_FAILED와_필드별_details를_준다() throws Exception {
		mockMvc.perform(post("/test/validation")
				.header("traceparent", TRACEPARENT)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{ "title": " ", "nickname": "이름이너무길어요" }
					"""))
			.andExpect(status().isBadRequest())
			.andExpect(content().json("""
				{
				  "success": false,
				  "data": null,
				  "error": {
				    "code": "VALIDATION_FAILED",
				    "message": "입력값을 확인해주세요.",
				    "details": [
				      { "field": "title", "reason": "제목을 입력해주세요." },
				      { "field": "nickname", "reason": "닉네임은 5자 이하로 입력해주세요." }
				    ],
				    "traceId": "%s"
				  }
				}
				""".formatted(TRACE_ID), JsonCompareMode.LENIENT))
			.andExpect(jsonPath("$.error.details.length()").value(2));
	}

	@Test
	void 없는_주소는_404_NOT_FOUND이고_details_키가_없다() throws Exception {
		mockMvc.perform(get("/test/unknown").header("traceparent", TRACEPARENT))
			.andExpect(status().isNotFound())
			.andExpect(content().json("""
				{
				  "success": false,
				  "data": null,
				  "error": { "code": "NOT_FOUND", "message": "요청한 주소를 찾을 수 없습니다.", "traceId": "%s" }
				}
				""".formatted(TRACE_ID), JsonCompareMode.STRICT));
	}

	@Test
	void 허용하지_않는_메서드는_405_METHOD_NOT_ALLOWED이다() throws Exception {
		mockMvc.perform(post("/test/success"))
			.andExpect(status().isMethodNotAllowed())
			.andExpect(jsonPath("$.error.code").value("METHOD_NOT_ALLOWED"))
			.andExpect(jsonPath("$.error.message").value("허용하지 않는 메서드입니다."));
	}

	@Test
	void 읽을_수_없는_본문은_400_INVALID_REQUEST이고_파서_오류_원문을_노출하지_않는다() throws Exception {
		String body = mockMvc.perform(post("/test/validation")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{ \"title\": "))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"))
			.andExpect(jsonPath("$.error.message").value("잘못된 요청입니다."))
			.andExpect(jsonPath("$.error.details").doesNotExist())
			.andReturn().getResponse().getContentAsString();

		assertThat(body).doesNotContain("JSON", "Exception", "tools.jackson");
	}

	@Test
	void 타입이_안_맞는_파라미터는_400_INVALID_REQUEST이다() throws Exception {
		mockMvc.perform(get("/test/param").param("count", "abc"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));
	}

	@Test
	void 필수_파라미터가_없으면_400_INVALID_REQUEST이다() throws Exception {
		mockMvc.perform(get("/test/param"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));
	}

	// 표에 없는 4xx는 상태코드를 유지하고 INVALID_REQUEST로 응답한다.
	@Test
	void 지원하지_않는_Content_Type은_415를_유지하고_INVALID_REQUEST로_응답한다() throws Exception {
		mockMvc.perform(post("/test/validation")
				.contentType(MediaType.TEXT_PLAIN)
				.content("title"))
			.andExpect(status().isUnsupportedMediaType())
			.andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));
	}

	@Test
	void 예상하지_못한_예외는_500이고_내부_정보를_노출하지_않는다() throws Exception {
		String body = mockMvc.perform(get("/test/unexpected").header("traceparent", TRACEPARENT))
			.andExpect(status().isInternalServerError())
			.andExpect(content().json("""
				{
				  "success": false,
				  "data": null,
				  "error": { "code": "INTERNAL_SERVER_ERROR", "message": "서버 내부 오류가 발생했습니다.", "traceId": "%s" }
				}
				""".formatted(TRACE_ID), JsonCompareMode.STRICT))
			.andReturn().getResponse().getContentAsString();

		assertThat(body).doesNotContain(INTERNAL_DETAIL, "IllegalStateException", "at com.nirvana");
	}

	record TestRequest(
		@NotBlank(message = "제목을 입력해주세요.")
		String title,
		@Size(max = 5, message = "닉네임은 5자 이하로 입력해주세요.")
		String nickname
	) {
	}

	@RestController
	static class TestController {

		@GetMapping("/test/success")
		ApiResponse<String> success() {
			return ApiResponse.success("ok");
		}

		@PostMapping("/test/validation")
		ApiResponse<String> validation(@Valid @RequestBody TestRequest request) {
			return ApiResponse.success(request.title());
		}

		@GetMapping("/test/param")
		ApiResponse<Integer> param(@RequestParam int count) {
			return ApiResponse.success(count);
		}

		@GetMapping("/test/unexpected")
		ApiResponse<Void> unexpected() {
			throw new IllegalStateException(INTERNAL_DETAIL);
		}
	}
}
