package com.nirvana.community.global.response;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import tools.jackson.databind.json.JsonMapper;

class ApiResponseTest {

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
	void failure_응답은_success_false_data_null_error_code_message로_직렬화된다() throws Exception {
		ApiResponse<Void> response = ApiResponse.failure("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.");

		String json = jsonMapper.writeValueAsString(response);

		JSONAssert.assertEquals("""
			{
			  "success": false,
			  "data": null,
			  "error": { "code": "INTERNAL_SERVER_ERROR", "message": "서버 내부 오류가 발생했습니다." }
			}
			""", json, true);
	}
}
