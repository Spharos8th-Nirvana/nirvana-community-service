package com.nirvana.community.domain.comment.entity;

import java.util.UUID;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.jspecify.annotations.Nullable;

import com.nirvana.community.global.auth.AccountType;
import com.nirvana.community.global.auth.Requester;
import com.nirvana.community.global.entity.BaseTimeEntity;
import com.nirvana.community.global.exception.BusinessException;
import com.nirvana.community.global.exception.ErrorCode;

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

// 댓글과 답글. 글(postId)과 부모 댓글(parentId)은 id로만 가리키고 연관관계는 매핑하지 않는다.
// 원댓글은 깊이 0, 답글은 부모 깊이 + 1이고 깊이 2까지만 있다(DDL CK_comments_depth). createdAt, updatedAt은 BaseTimeEntity가 채운다.
// @DynamicUpdate: 바뀐 컬럼만 UPDATE한다. 수정과 삭제·숨김이 동시에 와도 서로의 값(deleted, hidden)을 덮어쓰지 않는다.
@Getter
@Entity
@DynamicUpdate
@Table(name = "comments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseTimeEntity {

	private static final int ROOT_DEPTH = 0;
	private static final int MAX_DEPTH = 2;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// 밖에 보여 주는 식별자. 응답과 주소에는 id 대신 이 값을 쓴다. (COM-NFR-01)
	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(nullable = false, updatable = false, length = 36)
	private UUID uuid;

	@Column(nullable = false, updatable = false)
	private Long postId;

	// 원댓글은 NULL이다. (COM-CMT-02)
	@Column(updatable = false)
	private Long parentId;

	@Column(nullable = false, updatable = false)
	private int depth;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, updatable = false)
	private AccountType authorType;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(nullable = false, updatable = false, length = 36)
	private UUID authorUuid;

	// 작성 당시 표시 이름. 이름 변경 이벤트로 갱신될 수 있다. (COM-INT-01)
	@Column(nullable = false, length = 100)
	private String authorName;

	// 일반 텍스트 그대로 저장한다. HTML로 해석하지 않는다. (COM-NFR-04)
	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@Column(nullable = false)
	private boolean hidden;

	@Column(nullable = false)
	private boolean deleted;

	private Comment(Long postId, @Nullable Long parentId, int depth, Requester author, String authorName,
		String content) {
		this.uuid = UUID.randomUUID();
		this.postId = postId;
		this.parentId = parentId;
		this.depth = depth;
		this.authorType = author.accountType();
		this.authorUuid = author.accountUuid();
		this.authorName = authorName;
		this.content = content;
		this.hidden = false;
		this.deleted = false;
	}

	// 글에 원댓글을 만든다. 작성자는 요청자다. (COM-CMT-01, COM-BRD-02)
	public static Comment create(Long postId, Requester author, String authorName, String content) {
		return new Comment(postId, null, ROOT_DEPTH, author, authorName, content);
	}

	// 이 댓글에 답글을 만든다. 깊이는 이 댓글 깊이 + 1이다. (COM-CMT-02)
	// 같은 글의 댓글이어야 하고, 삭제·숨김된 댓글에는 답글을 달 수 없다(없는 댓글과 같이 거절한다).
	// 깊이 2인 댓글에는 답글을 달 수 없다.
	public Comment reply(Long postId, Requester author, String authorName, String content) {
		if (!this.postId.equals(postId) || deleted || hidden) {
			throw new BusinessException(ErrorCode.INVALID_PARENT_COMMENT);
		}
		if (depth >= MAX_DEPTH) {
			throw new BusinessException(ErrorCode.COMMENT_DEPTH_EXCEEDED);
		}
		return new Comment(postId, id, depth + 1, author, authorName, content);
	}
}
