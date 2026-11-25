package com.nirvana.community.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.nirvana.audit.creation.CreatedTime;
import com.nirvana.audit.modification.UpdatedTime;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

// 공통 라이브러리의 감사 타입을 넣은 Entity를 저장·수정할 때 시각이 채워지는지 확인한다. (COM-PST-08, COM-NFR-06)
// MySQL 없이 H2 메모리 DB로 실행한다. 메인 설정의 ddl-auto: validate 대신 테스트 Entity로 테이블을 만든다.
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Import(AuditingConfig.class)
class AuditingConfigTest {

	// 수정 시각이 저장 시각과 확실히 달라지도록 수정 전에 기다리는 시간
	private static final long WAIT_BEFORE_UPDATE_MILLIS = 10L;

	@Autowired
	private TestEntityManager entityManager;

	@Test
	void 저장하면_createdAt과_updatedAt이_채워진다() {
		TimeTestEntity saved = entityManager.persistFlushFind(new TimeTestEntity("처음"));

		assertThat(saved.creation.getCreatedAt()).isNotNull();
		assertThat(saved.modification.getUpdatedAt()).isNotNull();
	}

	@Test
	void 수정하면_updatedAt만_바뀌고_createdAt은_그대로다() throws InterruptedException {
		TimeTestEntity saved = entityManager.persistFlushFind(new TimeTestEntity("처음"));
		LocalDateTime createdAt = saved.creation.getCreatedAt();
		LocalDateTime updatedAt = saved.modification.getUpdatedAt();

		Thread.sleep(WAIT_BEFORE_UPDATE_MILLIS);
		saved.changeTitle("수정");
		entityManager.flush();
		entityManager.clear();
		TimeTestEntity updated = entityManager.find(TimeTestEntity.class, saved.id);

		assertThat(updated.creation.getCreatedAt()).isEqualTo(createdAt);
		assertThat(updated.modification.getUpdatedAt()).isAfter(updatedAt);
	}

	@Test
	void createdAt만_있는_Entity는_createdAt만_채워진다() {
		CreatedTestEntity saved = entityManager.persistFlushFind(new CreatedTestEntity());

		assertThat(saved.creation.getCreatedAt()).isNotNull();
	}

	// H2는 따옴표 없는 이름을 대문자로 저장하므로 대소문자는 무시하고, snake_case(created_at)로 바뀌지 않았는지 본다.
	@Test
	void 컬럼_이름은_DDL과_같은_camelCase다() {
		assertThat(columnNamesOf("TimeTestEntity"))
			.containsExactlyInAnyOrder("ID", "TITLE", "CREATEDAT", "UPDATEDAT");
		assertThat(columnNamesOf("CreatedTestEntity"))
			.containsExactlyInAnyOrder("ID", "CREATEDAT");
	}

	@SuppressWarnings("unchecked")
	private List<String> columnNamesOf(String tableName) {
		return entityManager.getEntityManager()
			.createNativeQuery("SELECT UPPER(COLUMN_NAME) FROM INFORMATION_SCHEMA.COLUMNS WHERE UPPER(TABLE_NAME) = UPPER(?1)")
			.setParameter(1, tableName)
			.getResultList();
	}

	@Entity(name = "TimeTestEntity")
	@EntityListeners(AuditingEntityListener.class)
	static class TimeTestEntity {

		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		private Long id;

		private String title;

		@Embedded
		private CreatedTime creation = new CreatedTime();

		@Embedded
		private UpdatedTime modification = new UpdatedTime();

		protected TimeTestEntity() {
		}

		TimeTestEntity(String title) {
			this.title = title;
		}

		void changeTitle(String title) {
			this.title = title;
		}
	}

	@Entity(name = "CreatedTestEntity")
	@EntityListeners(AuditingEntityListener.class)
	static class CreatedTestEntity {

		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		private Long id;

		@Embedded
		private CreatedTime creation = new CreatedTime();
	}
}
