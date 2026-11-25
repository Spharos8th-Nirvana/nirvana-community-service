package com.nirvana.community.domain.board.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.nirvana.community.domain.board.dto.BoardTypeResponse;
import com.nirvana.community.domain.board.dto.CreateBoardTypeRequest;
import com.nirvana.community.domain.board.dto.UpdateBoardTypeRequest;
import com.nirvana.community.domain.board.service.BoardTypeService;
import com.nirvana.community.global.auth.Requester;
import com.nirvana.community.global.exception.BusinessException;
import com.nirvana.community.global.exception.ErrorCode;

// Controller만 띄워 요청 검증과 응답 형식을 확인한다. Service는 가짜로 바꾼다. DB가 필요 없다.
@WebMvcTest(controllers = AdminBoardTypeController.class)
class AdminBoardTypeControllerTest {

	private static final String URL = "/api/v1/admin/boards";
	private static final String ADMIN_UUID = "3f2504e0-4f89-41d3-9a0c-0305e82c3301";
	private static final String VALID_BODY = """
		{
		  "code": "COMMUNITY", "name": "자유게시판",
		  "canSortOrder": false, "allowComment": true, "allowLike": true, "allowImage": true,
		  "allowScheduledPublish": false, "useCategory": true, "allowPin": false, "maxPinCount": 5
		}
		""";
	private static final BoardTypeResponse RESPONSE =
		new BoardTypeResponse("COMMUNITY", "자유게시판", false, true, true, true, false, true, false, 5);

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private BoardTypeService boardTypeService;

	@Test
	void 등록하면_201과_게시판_정보를_돌려준다() throws Exception {
		given(boardTypeService.create(any(Requester.class), any(CreateBoardTypeRequest.class))).willReturn(RESPONSE);

		mockMvc.perform(asAdmin(post(URL)).content(VALID_BODY))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.data.code").value("COMMUNITY"));
	}

	@Test
	void 상단_고정_최대_개수가_10을_넘으면_400과_필드별_오류() throws Exception {
		mockMvc.perform(asAdmin(post(URL)).content(VALID_BODY.replace("\"maxPinCount\": 5", "\"maxPinCount\": 11")))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"))
			.andExpect(jsonPath("$.error.details[0].field").value("maxPinCount"));
	}

	@Test
	void 코드가_소문자면_400() throws Exception {
		mockMvc.perform(asAdmin(post(URL)).content(VALID_BODY.replace("COMMUNITY", "community")))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.details[0].field").value("code"));
	}

	@Test
	void 기능_스위치를_빼면_400() throws Exception {
		mockMvc.perform(asAdmin(post(URL)).content(VALID_BODY.replace("\"allowComment\": true,", "")))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.details[0].field").value("allowComment"));
	}

	@Test
	void 관리자가_아니면_403() throws Exception {
		given(boardTypeService.create(any(Requester.class), any(CreateBoardTypeRequest.class)))
			.willThrow(new BusinessException(ErrorCode.FORBIDDEN));

		mockMvc.perform(post(URL)
				.header("X-User-Id", ADMIN_UUID)
				.header("X-Account-Type", "USER")
				.contentType(MediaType.APPLICATION_JSON)
				.content(VALID_BODY))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
	}

	@Test
	void 인증_헤더가_없으면_401() throws Exception {
		mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
	}

	@Test
	void 이미_있는_코드면_409() throws Exception {
		given(boardTypeService.create(any(Requester.class), any(CreateBoardTypeRequest.class)))
			.willThrow(new BusinessException(ErrorCode.DUPLICATE_BOARD_CODE));

		mockMvc.perform(asAdmin(post(URL)).content(VALID_BODY))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.error.code").value("DUPLICATE_BOARD_CODE"));
	}

	@Test
	void 수정하면_200과_바뀐_정보를_돌려준다() throws Exception {
		given(boardTypeService.update(any(Requester.class), eq("COMMUNITY"), any(UpdateBoardTypeRequest.class)))
			.willReturn(RESPONSE);

		mockMvc.perform(asAdmin(patch(URL + "/COMMUNITY")).content(VALID_BODY))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.name").value("자유게시판"));
	}

	@Test
	void 수정할_때_상단_고정_최대_개수가_0보다_작으면_400과_필드별_오류() throws Exception {
		mockMvc.perform(asAdmin(patch(URL + "/COMMUNITY"))
				.content(VALID_BODY.replace("\"maxPinCount\": 5", "\"maxPinCount\": -1")))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"))
			.andExpect(jsonPath("$.error.details[0].field").value("maxPinCount"));
	}

	@Test
	void 수정할_때_이름이_비어_있으면_400과_필드별_오류() throws Exception {
		mockMvc.perform(asAdmin(patch(URL + "/COMMUNITY"))
				.content(VALID_BODY.replace("\"name\": \"자유게시판\"", "\"name\": \"\"")))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"))
			.andExpect(jsonPath("$.error.details[0].field").value("name"));
	}

	@Test
	void 없는_게시판을_비활성화하면_404() throws Exception {
		willThrow(new BusinessException(ErrorCode.BOARD_NOT_FOUND))
			.given(boardTypeService).deactivate(any(Requester.class), eq("NOPE"));

		mockMvc.perform(asAdmin(post(URL + "/NOPE/deactivate")))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error.code").value("BOARD_NOT_FOUND"));
	}

	@Test
	void 비활성화하면_204() throws Exception {
		mockMvc.perform(asAdmin(post(URL + "/COMMUNITY/deactivate")))
			.andExpect(status().isNoContent());

		verify(boardTypeService).deactivate(any(Requester.class), eq("COMMUNITY"));
	}

	private MockHttpServletRequestBuilder asAdmin(MockHttpServletRequestBuilder request) {
		return request
			.header("X-User-Id", ADMIN_UUID)
			.header("X-Account-Type", "ADMIN")
			.contentType(MediaType.APPLICATION_JSON);
	}
}
