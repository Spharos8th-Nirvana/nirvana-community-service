package com.nirvana.community.domain.board.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.nirvana.community.domain.board.dto.BoardTypeResponse;
import com.nirvana.community.domain.board.service.BoardTypeService;
import com.nirvana.community.global.auth.AccountType;
import com.nirvana.community.global.auth.Requester;

// Controller만 띄운다. Service는 가짜로 바꾼다. DB가 필요 없다.
@WebMvcTest(controllers = BoardTypeController.class)
class BoardTypeControllerTest {

	private static final String USER_UUID = "3f2504e0-4f89-41d3-9a0c-0305e82c3301";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private BoardTypeService boardTypeService;

	@Test
	void 로그인하지_않아도_게시판_목록을_볼_수_있다() throws Exception {
		given(boardTypeService.getActiveBoardTypes(null)).willReturn(List.of(
			new BoardTypeResponse("COMMUNITY", "자유게시판", false, true, true, true, false, true, false, 5, true, false)));

		mockMvc.perform(get("/api/v1/boards"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data[0].code").value("COMMUNITY"))
			.andExpect(jsonPath("$.data[0].useCategory").value(true))
			.andExpect(jsonPath("$.data[0].maxPinCount").value(5))
			.andExpect(jsonPath("$.data[0].canRead").value(true))
			.andExpect(jsonPath("$.data[0].canWrite").value(false));
	}

	@Test
	void 로그인하면_요청자_정보로_열람_작성_가능_여부를_계산한다() throws Exception {
		Requester user = new Requester(UUID.fromString(USER_UUID), AccountType.USER);
		given(boardTypeService.getActiveBoardTypes(user)).willReturn(List.of(
			new BoardTypeResponse("COMMUNITY", "자유게시판", false, true, true, true, false, true, false, 5, true, true)));

		mockMvc.perform(get("/api/v1/boards").header("X-User-Id", USER_UUID).header("X-Account-Type", "USER"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].canWrite").value(true));
	}

	@Test
	void 인증_헤더가_하나만_있으면_401() throws Exception {
		mockMvc.perform(get("/api/v1/boards").header("X-Account-Type", "USER"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
	}
}
