package com.nirvana.community.domain.post.repository;

import java.util.Optional;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nirvana.community.domain.post.dto.MyPostSummaryRow;
import com.nirvana.community.domain.post.dto.PostSummaryRow;
import com.nirvana.community.domain.post.entity.Post;
import com.nirvana.community.domain.post.entity.PostStatus;
import com.nirvana.community.global.auth.AccountType;

public interface PostRepository extends JpaRepository<Post, Long> {

	// 단건 조회: 게시글 상세·수정·삭제에서 쓴다. 상태·숨김·삭제와 관계없이 읽는다. 누가 볼 수 있는지는 Post가 판단한다. (COM-PST-23, 4.5)
	Optional<Post> findByUuid(UUID uuid);

	// 다건 조회: 게시판 글 목록에서 쓴다. 공개 상태이고 숨김·삭제되지 않은 글만 읽는다. (COM-PST-20, 4.5, COM-NFR-05)
	// categoryId가 null이면 카테고리로 거르지 않는다. 정렬은 부르는 쪽이 Pageable로 넘긴다.
	// 목록에는 본문이 필요 없어 Post 전체 대신 필요한 컬럼만 고른다. (COM-PST-26)
	@Query("""
		select new com.nirvana.community.domain.post.dto.PostSummaryRow(
			p.uuid, p.title, p.authorName, p.categoryId, p.thumbnailMediaUuid, p.scheduledAt)
		from Post p
		where p.typeId = :typeId
			and p.status = com.nirvana.community.domain.post.entity.PostStatus.PUBLISHED
			and p.hidden = false
			and p.deleted = false
			and (:categoryId is null or p.categoryId = :categoryId)
		""")
	Page<PostSummaryRow> findVisibleSummaries(@Param("typeId") Long typeId,
		@Param("categoryId") @Nullable Long categoryId, Pageable pageable);

	// 다건 조회: 내가 쓴 글 목록에서 쓴다. 작성자(UUID와 종류)가 같은 글을 상태와 관계없이 읽고, 삭제한 글만 뺀다.
	// 숨김 글도 읽는다(작성자는 자기 숨김 글을 본다). status가 null이면 상태로 거르지 않는다. (COM-PST-25, 4.5)
	// 정렬은 부르는 쪽이 Pageable로 넘긴다. 본문은 읽지 않는다. (COM-PST-26)
	@Query("""
		select new com.nirvana.community.domain.post.dto.MyPostSummaryRow(
			p.uuid, p.title, p.authorName, p.typeId, p.categoryId, p.thumbnailMediaUuid, p.status, p.hidden,
			p.scheduledAt)
		from Post p
		where p.authorUuid = :authorUuid
			and p.authorType = :authorType
			and p.deleted = false
			and (:status is null or p.status = :status)
		""")
	Page<MyPostSummaryRow> findMySummaries(@Param("authorUuid") UUID authorUuid,
		@Param("authorType") AccountType authorType, @Param("status") @Nullable PostStatus status,
		Pageable pageable);
}
