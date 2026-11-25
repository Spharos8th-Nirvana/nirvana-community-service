package com.nirvana.community.domain.board.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.nirvana.community.domain.board.dto.BoardTypeResponse;
import com.nirvana.community.domain.board.dto.CreateBoardTypeRequest;
import com.nirvana.community.domain.board.dto.UpdateBoardTypeRequest;
import com.nirvana.community.domain.board.entity.BoardFeatures;
import com.nirvana.community.domain.board.entity.BoardPermission;
import com.nirvana.community.domain.board.entity.BoardRole;
import com.nirvana.community.domain.board.entity.BoardType;
import com.nirvana.community.domain.board.repository.BoardPermissionRepository;
import com.nirvana.community.domain.board.repository.BoardTypeRepository;
import com.nirvana.community.global.auth.AccountType;
import com.nirvana.community.global.auth.Requester;
import com.nirvana.community.global.exception.BusinessException;
import com.nirvana.community.global.exception.ErrorCode;

// Repository를 가짜(Mock)로 바꿔 Service의 규칙만 확인한다. DB가 필요 없다.
@ExtendWith(MockitoExtension.class)
class BoardTypeServiceTest {

	private static final Requester ADMIN = new Requester(UUID.randomUUID(), AccountType.ADMIN);
	private static final Requester USER = new Requester(UUID.randomUUID(), AccountType.USER);
	private static final CreateBoardTypeRequest CREATE_REQUEST = new CreateBoardTypeRequest(
		"COMMUNITY", "자유게시판", false, true, true, true, false, true, false, 5);
	private static final UpdateBoardTypeRequest UPDATE_REQUEST = new UpdateBoardTypeRequest(
		"이야기", false, false, true, true, false, true, false, 3);

	@Mock
	private BoardTypeRepository boardTypeRepository;
	@Mock
	private BoardPermissionRepository boardPermissionRepository;
	@InjectMocks
	private BoardTypeService boardTypeService;

	@Test
	void 운영_중인_게시판_목록을_응답으로_바꿔_준다() {
		given(boardTypeRepository.findAllByActiveTrueOrderByIdAsc()).willReturn(List.of(savedBoardType()));

		List<BoardTypeResponse> responses = boardTypeService.getActiveBoardTypes(null);

		assertThat(responses).hasSize(1);
		assertThat(responses.get(0).code()).isEqualTo("COMMUNITY");
		assertThat(responses.get(0).useCategory()).isTrue();
	}

	@Test
	void 목록에는_요청자_역할의_권한을_한_번에_읽어_붙이고_행이_없는_게시판은_둘_다_false() {
		BoardType community = savedBoardType(1L);
		BoardType notice = savedBoardType(2L);
		given(boardTypeRepository.findAllByActiveTrueOrderByIdAsc()).willReturn(List.of(community, notice));
		given(boardPermissionRepository.findAllByRole(BoardRole.USER)).willReturn(List.of(
			BoardPermission.create(1L, BoardRole.USER, true, true, UUID.randomUUID())));

		List<BoardTypeResponse> responses = boardTypeService.getActiveBoardTypes(USER);

		assertThat(responses).extracting(BoardTypeResponse::canRead).containsExactly(true, false);
		assertThat(responses).extracting(BoardTypeResponse::canWrite).containsExactly(true, false);
		verify(boardPermissionRepository, times(1)).findAllByRole(BoardRole.USER);
	}

	@Test
	void 비회원의_목록은_GUEST_권한으로_계산한다() {
		given(boardTypeRepository.findAllByActiveTrueOrderByIdAsc()).willReturn(List.of(savedBoardType(1L)));
		given(boardPermissionRepository.findAllByRole(BoardRole.GUEST)).willReturn(List.of(
			BoardPermission.create(1L, BoardRole.GUEST, true, false, UUID.randomUUID())));

		List<BoardTypeResponse> responses = boardTypeService.getActiveBoardTypes(null);

		assertThat(responses.get(0).canRead()).isTrue();
		assertThat(responses.get(0).canWrite()).isFalse();
	}

	@Test
	void 관리자가_아니면_등록_수정_비활성화_모두_FORBIDDEN() {
		assertFails(() -> boardTypeService.create(USER, CREATE_REQUEST), ErrorCode.FORBIDDEN);
		assertFails(() -> boardTypeService.update(USER, "COMMUNITY", UPDATE_REQUEST), ErrorCode.FORBIDDEN);
		assertFails(() -> boardTypeService.deactivate(USER, "COMMUNITY"), ErrorCode.FORBIDDEN);
		verify(boardTypeRepository, never()).save(any());
	}

	@Test
	void 이미_있는_코드면_DUPLICATE_BOARD_CODE() {
		given(boardTypeRepository.existsByCode("COMMUNITY")).willReturn(true);

		assertFails(() -> boardTypeService.create(ADMIN, CREATE_REQUEST), ErrorCode.DUPLICATE_BOARD_CODE);
		verify(boardTypeRepository, never()).save(any());
	}

	@Test
	void 등록하면_저장하고_등록한_관리자를_남긴다() {
		given(boardTypeRepository.existsByCode("COMMUNITY")).willReturn(false);

		BoardTypeResponse response = boardTypeService.create(ADMIN, CREATE_REQUEST);

		ArgumentCaptor<BoardType> saved = ArgumentCaptor.forClass(BoardType.class);
		verify(boardTypeRepository).save(saved.capture());
		assertThat(saved.getValue().getUpdatedBy()).isEqualTo(ADMIN.accountUuid());
		assertThat(saved.getValue().isActive()).isTrue();
		assertThat(response.code()).isEqualTo("COMMUNITY");
		assertThat(response.maxPinCount()).isEqualTo(5);
	}

	@Test
	void 없는_게시판을_수정하거나_비활성화하면_BOARD_NOT_FOUND() {
		given(boardTypeRepository.findByCode("NOPE")).willReturn(Optional.empty());

		assertFails(() -> boardTypeService.update(ADMIN, "NOPE", UPDATE_REQUEST), ErrorCode.BOARD_NOT_FOUND);
		assertFails(() -> boardTypeService.deactivate(ADMIN, "NOPE"), ErrorCode.BOARD_NOT_FOUND);
	}

	@Test
	void 수정하면_이름과_기능_스위치가_바뀐다() {
		BoardType boardType = savedBoardType();
		given(boardTypeRepository.findByCode("COMMUNITY")).willReturn(Optional.of(boardType));

		BoardTypeResponse response = boardTypeService.update(ADMIN, "COMMUNITY", UPDATE_REQUEST);

		assertThat(response.name()).isEqualTo("이야기");
		assertThat(response.allowComment()).isFalse();
		assertThat(boardType.getUpdatedBy()).isEqualTo(ADMIN.accountUuid());
	}

	@Test
	void 비활성화하면_게시판이_꺼진다() {
		BoardType boardType = savedBoardType();
		given(boardTypeRepository.findByCode("COMMUNITY")).willReturn(Optional.of(boardType));

		boardTypeService.deactivate(ADMIN, "COMMUNITY");

		assertThat(boardType.isActive()).isFalse();
		assertThat(boardType.getUpdatedBy()).isEqualTo(ADMIN.accountUuid());
	}

	private BoardType savedBoardType(Long id) {
		BoardType boardType = savedBoardType();
		ReflectionTestUtils.setField(boardType, "id", id);
		return boardType;
	}

	private BoardType savedBoardType() {
		return BoardType.create("COMMUNITY", "자유게시판",
			new BoardFeatures(false, true, true, true, false, true, false, 5), UUID.randomUUID());
	}

	private void assertFails(ThrowingCallable call, ErrorCode expected) {
		assertThatThrownBy(call)
			.isInstanceOf(BusinessException.class)
			.extracting(exception -> ((BusinessException) exception).getErrorCode())
			.isEqualTo(expected);
	}
}
