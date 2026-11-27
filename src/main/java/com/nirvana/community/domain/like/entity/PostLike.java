package com.nirvana.community.domain.like.entity;

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

// 회원 한 명이 글 하나에 누른 좋아요. 회원·글마다 행은 하나(UNIQUE postId, userUuid)이고 취소해도 지우지 않는다.
// 누르기·취소는 liked만 켜고 끈다. 다시 누르면 같은 행을 다시 켠다. (COM-LIK-01, COM-LIK-02, COM-LIK-04)
// 글과 생명주기가 달라 posts와 연관관계를 매핑하지 않고 id만 저장한다. createdAt, updatedAt은 BaseTimeEntity가 채운다.
@Getter
@Entity
@Table(name = "postLikes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostLike extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, updatable = false)
	private Long postId;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(nullable = false, updatable = false, length = 36)
	private UUID userUuid;

	@Column(nullable = false)
	private boolean liked;

	private PostLike(Long postId, UUID userUuid) {
		this.postId = postId;
		this.userUuid = userUuid;
		this.liked = true;
	}

	// 처음 누른 좋아요. 켜진 상태로 만든다.
	public static PostLike create(Long postId, UUID userUuid) {
		return new PostLike(postId, userUuid);
	}

	// 좋아요를 켠다. 이미 켜져 있으면 그대로 두고 false를 준다. 좋아요 수는 true일 때만 올린다. (COM-LIK-04)
	public boolean like() {
		if (liked) {
			return false;
		}
		this.liked = true;
		return true;
	}

	// 좋아요를 끈다. 이미 꺼져 있으면 그대로 두고 false를 준다. 좋아요 수는 true일 때만 내린다. (COM-LIK-02, COM-LIK-04)
	public boolean unlike() {
		if (!liked) {
			return false;
		}
		this.liked = false;
		return true;
	}
}
