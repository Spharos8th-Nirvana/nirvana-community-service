package com.nirvana.community.global.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// application.yml의 spring.data.web.pageable 값이 실제 요청 처리에 적용되는지 확인한다.
// MVC 슬라이스만 띄우므로 DB 연결이 필요 없다.
@WebMvcTest(controllers = PageableSettingsTest.PageableEchoController.class)
@Import(PageableSettingsTest.PageableEchoController.class)
class PageableSettingsTest {

	private static final int FIRST_PAGE = 0;
	private static final int DEFAULT_PAGE_SIZE = 20;
	private static final int MAX_PAGE_SIZE = 100;

	@Autowired
	private MockMvc mockMvc;

	@Test
	void page와_size를_보내지_않으면_page_0_size_20으로_처리된다() throws Exception {
		mockMvc.perform(get("/test/pageable"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.page").value(FIRST_PAGE))
			.andExpect(jsonPath("$.size").value(DEFAULT_PAGE_SIZE));
	}

	@Test
	void size가_100을_넘으면_100으로_처리된다() throws Exception {
		mockMvc.perform(get("/test/pageable").param("size", "1000"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.size").value(MAX_PAGE_SIZE));
	}

	@Test
	void page는_0부터_시작하는_값을_그대로_받는다() throws Exception {
		mockMvc.perform(get("/test/pageable").param("page", "2").param("size", "30"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.page").value(2))
			.andExpect(jsonPath("$.size").value(30));
	}

	@RestController
	static class PageableEchoController {

		@GetMapping("/test/pageable")
		Map<String, Integer> echo(Pageable pageable) {
			return Map.of("page", pageable.getPageNumber(), "size", pageable.getPageSize());
		}
	}
}
