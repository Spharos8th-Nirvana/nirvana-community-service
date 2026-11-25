package com.nirvana.community.global.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

// 생성 시각(createdAt)만 있는 테이블용 기반 클래스. 예: reports, reviewEligibilities, postMedia, outboxEvents
// 저장할 때 JPA Auditing이 값을 채우고, 이후에는 바꾸지 않는다.
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseCreatedEntity {

	@CreatedDate
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;
}
