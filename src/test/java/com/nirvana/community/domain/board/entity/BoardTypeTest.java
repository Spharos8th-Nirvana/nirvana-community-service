package com.nirvana.community.domain.board.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class BoardTypeTest {

	private static final UUID ADMIN_UUID = UUID.randomUUID();
	private static final BoardFeatures FEATURES = new BoardFeatures(false, true, true, true, false, true, false, 5);

	@Test
	void 새_게시판은_운영_중이고_등록한_관리자가_남는다() {
		BoardType boardType = BoardType.create("COMMUNITY", "자유게시판", FEATURES, ADMIN_UUID);

		assertThat(boardType.getUuid()).isNotNull();
		assertThat(boardType.getCode()).isEqualTo("COMMUNITY");
		assertThat(boardType.isActive()).isTrue();
		assertThat(boardType.isUseCategory()).isTrue();
		assertThat(boardType.getMaxPinCount()).isEqualTo(5);
		assertThat(boardType.getUpdatedBy()).isEqualTo(ADMIN_UUID);
	}

	@Test
	void 수정하면_이름과_기능_스위치가_바뀌고_코드는_그대로다() {
		BoardType boardType = BoardType.create("COMMUNITY", "자유게시판", FEATURES, ADMIN_UUID);
		UUID otherAdmin = UUID.randomUUID();

		boardType.update("이야기", new BoardFeatures(true, false, false, false, true, false, true, 3), otherAdmin);

		assertThat(boardType.getCode()).isEqualTo("COMMUNITY");
		assertThat(boardType.getName()).isEqualTo("이야기");
		assertThat(boardType.isAllowComment()).isFalse();
		assertThat(boardType.isAllowPin()).isTrue();
		assertThat(boardType.getMaxPinCount()).isEqualTo(3);
		assertThat(boardType.getUpdatedBy()).isEqualTo(otherAdmin);
	}

	@Test
	void 비활성화하면_꺼지고_처리한_관리자가_남는다() {
		BoardType boardType = BoardType.create("COMMUNITY", "자유게시판", FEATURES, ADMIN_UUID);
		UUID otherAdmin = UUID.randomUUID();

		boardType.deactivate(otherAdmin);

		assertThat(boardType.isActive()).isFalse();
		assertThat(boardType.getUpdatedBy()).isEqualTo(otherAdmin);
	}
}
