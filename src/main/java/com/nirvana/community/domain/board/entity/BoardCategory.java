package com.nirvana.community.domain.board.entity;

import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.nirvana.community.global.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 게시판 카테고리(기준정보). 게시판은 typeId로만 가리키고 연관관계는 매핑하지 않는다.
// createdAt, updatedAt은 BaseTimeEntity가 채운다.
@Getter
@Entity
@Table(name = "boardCategories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardCategory extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(nullable = false, updatable = false, length = 36)
	private UUID uuid;

	// 카테고리를 다른 게시판으로 옮기지 않는다. (COM-BRD-21)
	@Column(nullable = false, updatable = false)
	private Long typeId;

	@Column(nullable = false, length = 100)
	private String name;

	// 보관하면 false. 행은 지우지 않는다. (COM-BRD-22)
	@Column(nullable = false)
	private boolean active;

	@Column(nullable = false)
	private int sortOrder;

	// 마지막으로 바꾼 관리자. 컬럼은 NULL을 허용하지만 이 서비스가 저장할 때는 항상 채운다. (COM-NFR-06)
	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(length = 36)
	private UUID updatedBy;

	private BoardCategory(Long typeId, String name, int sortOrder, UUID createdBy) {
		this.uuid = UUID.randomUUID();
		this.typeId = typeId;
		this.name = name;
		this.sortOrder = sortOrder;
		this.active = true;
		this.updatedBy = createdBy;
	}

	public static BoardCategory create(Long typeId, String name, int sortOrder, UUID createdBy) {
		return new BoardCategory(typeId, name, sortOrder, createdBy);
	}

	// 이름과 순서만 바꾼다. (COM-BRD-21)
	public void update(String name, int sortOrder, UUID updatedBy) {
		this.name = name;
		this.sortOrder = sortOrder;
		this.updatedBy = updatedBy;
	}

	// 삭제하지 않고 보관한다. 이미 보관된 것을 다시 보관해도 결과는 같다. (COM-BRD-22)
	public void archive(UUID updatedBy) {
		this.active = false;
		this.updatedBy = updatedBy;
	}

	// 보관한 카테고리를 다시 쓴다. 이미 쓰는 중이어도 결과는 같다. (COM-BRD-33)
	public void restore(UUID updatedBy) {
		this.active = true;
		this.updatedBy = updatedBy;
	}

	// 그 게시판의 카테고리인지 확인한다. 보관 여부는 보지 않는다(보관된 카테고리의 기존 글도 보인다). (COM-BRD-22)
	public boolean belongsTo(BoardType boardType) {
		return typeId.equals(boardType.getId());
	}

	// 그 게시판의 사용 중인 카테고리인지 확인한다. 보관된 카테고리로는 새 글을 쓸 수 없다. (COM-PST-02, COM-BRD-22)
	public boolean isUsableIn(BoardType boardType) {
		return active && typeId.equals(boardType.getId());
	}
}
