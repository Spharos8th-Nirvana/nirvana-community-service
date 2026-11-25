package com.nirvana.community.global.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

// 생성 시각(createdAt)과 수정 시각(updatedAt)이 있는 테이블용 기반 클래스. (COM-PST-08, COM-NFR-06)
// 수정 시각은 Entity가 바뀌어 저장될 때마다 JPA Auditing이 갱신한다.
@Getter
@MappedSuperclass
public abstract class BaseTimeEntity extends BaseCreatedEntity {

	@LastModifiedDate
	@Column(nullable = false)
	private LocalDateTime updatedAt;
}
