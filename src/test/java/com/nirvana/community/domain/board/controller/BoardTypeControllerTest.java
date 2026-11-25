package com.nirvana.community.domain.board.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.nirvana.community.domain.board.dto.BoardTypeResponse;
import com.nirvana.community.domain.board.service.BoardTypeService;

// Controller만 띄운다. Service는 가짜로 바꾼다. DB가 필요 없다.
@WebMvcTest(controllers = BoardTypeController.class)
class BoardTypeControllerTest {

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
			.andExpect(jsonPath("$.data[0].maxPinCount").value(5));
	}
}
