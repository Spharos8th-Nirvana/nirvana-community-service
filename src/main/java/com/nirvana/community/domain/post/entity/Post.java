package com.nirvana.community.domain.post.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.jspecify.annotations.Nullable;

import com.nirvana.community.global.auth.AccountType;
import com.nirvana.community.global.auth.Requester;
import com.nirvana.community.global.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 게시글 루트. 게시판·카테고리·사찰 탭은 id로만 가리키고 연관관계는 매핑하지 않는다.
// createdAt, updatedAt은 BaseTimeEntity가 채운다.
@Getter
@Entity
@Table(name = "posts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// 밖에 보여 주는 식별자. 응답과 주소에는 id 대신 이 값을 쓴다. (COM-NFR-01)
	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(nullable = false, updatable = false, length = 36)
	private UUID uuid;

	@Column(nullable = false, updatable = false)
	private Long typeId;

	// 카테고리를 쓰지 않는 게시판의 글은 NULL이다. (COM-PST-02)
	private Long categoryId;

	// NULL이면 플랫폼 글이다. 사찰 게시판 글은 COM-PST-03에서 다룬다.
	@Column(updatable = false)
	private Long templeBoardId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, updatable = false)
	private AccountType authorType;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(nullable = false, updatable = false, length = 36)
	private UUID authorUuid;

	// 작성 당시 표시 이름. 이름 변경 이벤트로 갱신될 수 있다. (COM-INT-01)
	@Column(nullable = false, length = 100)
	private String authorName;

	@Column(nullable = false, length = 200)
	private String title;

	@Column(nullable = false, columnDefinition = "MEDIUMTEXT")
	private String content;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(length = 36)
	private UUID thumbnailMediaUuid;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PostStatus status;

	// 공개된 시각. 목록 정렬과 화면의 작성 일시 기준이다. 임시저장 글은 NULL이다. (COM-PST-14)
	private LocalDateTime scheduledAt;

	@Column(nullable = false)
	private boolean hidden;

	private LocalDateTime hiddenAt;

	@Column(nullable = false)
	private boolean deleted;

	// 마지막으로 수정한 사람의 구분. 새 글은 NULL이다. (COM-PST-08)
	@Enumerated(EnumType.STRING)
	private AccountType updateByType;

	private Post(Long typeId, @Nullable Long categoryId, Requester author, String authorName, String title,
		String content, LocalDateTime publishedAt) {
		this.uuid = UUID.randomUUID();
		this.typeId = typeId;
		this.categoryId = categoryId;
		this.authorType = author.accountType();
		this.authorUuid = author.accountUuid();
		this.authorName = authorName;
		this.title = title;
		this.content = content;
		this.status = PostStatus.PUBLISHED;
		this.scheduledAt = publishedAt;
		this.hidden = false;
		this.deleted = false;
	}

	// 바로 공개되는 플랫폼 글을 만든다. 작성자는 요청자다. (COM-PST-01, COM-BRD-02)
	public static Post create(Long typeId, @Nullable Long categoryId, Requester author, String authorName,
		String title, String content, LocalDateTime publishedAt) {
		return new Post(typeId, categoryId, author, authorName, title, content, publishedAt);
	}

	// 이 요청자에게 글이 있는 것으로 보이는지. false면 없는 글(404)로 응답한다. (4.5)
	// 임시저장·예약 글은 아직 공개된 적이 없어 작성자 본인에게만 있다. ADMIN도 남의 것은 못 본다.
	// 숨김·삭제보다 먼저 본다: 남의 임시저장 글은 삭제·숨김이어도 "볼 수 없는 글"이 아니라 404여야 글이 있다는 것조차 새지 않는다.
	public boolean isVisibleTo(@Nullable Requester requester) {
		return status == PostStatus.PUBLISHED || isWrittenBy(requester);
	}

	// 본문까지 볼 수 있는지. false면 본문 없이 "볼 수 없는 글"로 응답한다. (COM-PST-24, COM-PST-09, 4.5)
	// 삭제 글은 작성자·ADMIN을 포함해 아무도 못 본다(삭제가 숨김보다 우선).
	// 숨김 글은 ADMIN과 작성자 본인만 본다. 작성자는 자기 글이 왜 안 보이는지 확인할 수 있어야 한다.
	public boolean isContentViewableBy(@Nullable Requester requester) {
		if (deleted) {
			return false;
		}
		if (hidden) {
			return isAdmin(requester) || isWrittenBy(requester);
		}
		return true;
	}

	// 요청자가 이 글의 작성자인지. 비회원(null)은 작성자가 아니다. 다른 계정 종류끼리 UUID가 겹쳐도 구분되게 종류도 본다.
	public boolean isWrittenBy(@Nullable Requester requester) {
		return requester != null
			&& authorUuid.equals(requester.accountUuid())
			&& authorType == requester.accountType();
	}

	private boolean isAdmin(@Nullable Requester requester) {
		return requester != null && requester.accountType() == AccountType.ADMIN;
	}
}
