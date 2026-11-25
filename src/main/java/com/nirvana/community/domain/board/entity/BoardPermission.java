package com.nirvana.community.domain.board.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 게시판 종류별 역할의 열람·작성 권한(기준정보). 행이 없으면 열람·작성 모두 거절한다. (COM-BRD-01, COM-BRD-13)
// 테이블에 createdAt이 없어 BaseTimeEntity를 상속하지 않고 updatedAt만 직접 둔다.
@Getter
@Entity
@Table(name = "boardPermissions")
@IdClass(BoardPermissionId.class)
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardPermission {

	@Id
	private Long boardTypeId;

	@Id
	@Enumerated(EnumType.STRING)
	private BoardRole role;

	@Column(nullable = false)
	private boolean canRead;

	@Column(nullable = false)
	private boolean canWrite;

	@LastModifiedDate
	@Column(nullable = false)
	private LocalDateTime updatedAt;

	// 마지막으로 권한을 바꾼 관리자. (COM-NFR-06)
	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(length = 36)
	private UUID updatedBy;

	private BoardPermission(Long boardTypeId, BoardRole role, boolean canRead, boolean canWrite, UUID updatedBy) {
		this.boardTypeId = boardTypeId;
		this.role = role;
		this.canRead = canRead;
		this.canWrite = canWrite;
		this.updatedBy = updatedBy;
	}

	public static BoardPermission create(Long boardTypeId, BoardRole role, boolean canRead, boolean canWrite,
		UUID createdBy) {
		return new BoardPermission(boardTypeId, role, canRead, canWrite, createdBy);
	}

	public void change(boolean canRead, boolean canWrite, UUID updatedBy) {
		this.canRead = canRead;
		this.canWrite = canWrite;
		this.updatedBy = updatedBy;
	}
}
