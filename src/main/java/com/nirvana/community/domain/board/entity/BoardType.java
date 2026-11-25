package com.nirvana.community.domain.board.entity;


import com.nirvana.community.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

// 게시판 종류(기준정보). createdAt, updatedAt은 BaseTimeEntity가 채운다.
@Getter
@Entity
@Table(name = "boardTypes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardType extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@JdbcTypeCode(SqlTypes.CHAR) //해당 필드를 JDBC SQL 타입 CHAR로 매핑하라고 명시
	@Column(nullable = false, updatable = false, length = 36)
	private UUID uuid;

	// 등록 후 바꾸지 않는다. (COM-BRD-11)
	@Column(nullable = false, updatable = false, length = 30)
	private String code;

	@Column(nullable = false, length = 50)
	private String name;

	@Column(nullable = false)
	private boolean canSortOrder;

	@Column(nullable = false)
	private boolean allowComment;

	@Column(nullable = false)
	private boolean allowLike;

	@Column(nullable = false)
	private boolean allowImage;

	@Column(nullable = false)
	private boolean allowScheduledPublish;

	@Column(nullable = false)
	private boolean useCategory;

	@Column(nullable = false)
	private boolean allowPin;

	@Column(nullable = false)
	private int maxPinCount;

	@Column(nullable = false)
	private boolean active;

	// 마지막으로 설정을 바꾼 관리자. 수정·비활성화 때마다 바뀌므로 updatable = false를 두지 않는다. (COM-NFR-06)
	@JdbcTypeCode(SqlTypes.CHAR) //해당 필드를 JDBC SQL 타입 CHAR로 매핑하라고 명시
	@Column(nullable = false, length = 36)
	private UUID updatedBy;

	private BoardType(String code, String name, BoardFeatures features, UUID createdBy) {
		this.uuid = UUID.randomUUID();
		this.code = code;
		this.name = name;
		this.active = true;
		applyFeatures(features);
		this.updatedBy = createdBy;
	}

	public static BoardType create(String code, String name, BoardFeatures features, UUID createdBy) {
		return new BoardType(code, name, features, createdBy);
	}

	// 이름과 기능 스위치만 바꾼다. code는 바꾸지 않는다. (COM-BRD-11)
	public void update(String name, BoardFeatures features, UUID updatedBy) {
		this.name = name;
		applyFeatures(features);
		this.updatedBy = updatedBy;
	}

	// 삭제하지 않고 끈다. (COM-BRD-12)
	public void deactivate(UUID updatedBy) {
		this.active = false;
		this.updatedBy = updatedBy;
	}

	private void applyFeatures(BoardFeatures features) {
		this.canSortOrder = features.canSortOrder();
		this.allowComment = features.allowComment();
		this.allowLike = features.allowLike();
		this.allowImage = features.allowImage();
		this.allowScheduledPublish = features.allowScheduledPublish();
		this.useCategory = features.useCategory();
		this.allowPin = features.allowPin();
		this.maxPinCount = features.maxPinCount();
	}

}
