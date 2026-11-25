package com.nirvana.community.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.nirvana.audit.config.JpaAuditingConfig;

// 팀 공통 라이브러리(nirvana-jpa-auditing)의 JPA Auditing 설정을 켠다. createdAt, updatedAt을 Asia/Seoul 시각으로 자동 기록한다.
// 메인 애플리케이션 클래스가 아니라 따로 두어 @WebMvcTest 같은 슬라이스 테스트가 JPA 없이 뜨게 한다.
@Configuration
@Import(JpaAuditingConfig.class)
public class AuditingConfig {
}
