package com.nirvana.community.global.response;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import tools.jackson.databind.json.JsonMapper;

class ApiResponseTest {

	private static final String TRACE_ID = "4bf92f3577b34da6a3ce929d0e0e4736";

	private final JsonMapper jsonMapper = JsonMapper.builder().build();

	@Test
	void success_응답은_success_true_data_error_null로_직렬화된다() throws Exception {
		ApiResponse<Map<String, String>> response = ApiResponse.success(Map.of("postUuid", "post-001"));

		String json = jsonMapper.writeValueAsString(response);

		JSONAssert.assertEquals("""
			{
			  "success": true,
			  "data": { "postUuid": "post-001" },
			  "error": null
			}
			""", json, true);
	}

	@Test
	void failure_응답은_success_false_data_null_error_code_message_traceId로_직렬화된다() throws Exception {
		ErrorResponse error = new ErrorResponse("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.", TRACE_ID);
		ApiResponse<Void> response = ApiResponse.failure(error);

		String json = jsonMapper.writeValueAsString(response);

		JSONAssert.assertEquals("""
			{
			  "success": false,
			  "data": null,
			  "error": {
			    "code": "INTERNAL_SERVER_ERROR",
			    "message": "서버 내부 오류가 발생했습니다.",
			    "traceId": "%s"
			  }
			}
			""".formatted(TRACE_ID), json, true);
	}

	@Test
	void traceId가_없으면_null로_직렬화된다() throws Exception {
		ApiResponse<Void> response = ApiResponse.failure(new ErrorResponse("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.", null));

		String json = jsonMapper.writeValueAsString(response);

		JSONAssert.assertEquals("""
			{
			  "success": false,
			  "data": null,
			  "error": { "code": "INTERNAL_SERVER_ERROR", "message": "서버 내부 오류가 발생했습니다.", "traceId": null }
			}
			""", json, true);
	}
}
