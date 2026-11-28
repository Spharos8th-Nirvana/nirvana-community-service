package com.nirvana.community.domain.comment.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nirvana.community.domain.comment.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {

	// 단건 조회: 답글 작성의 부모 댓글, 댓글 수정·삭제에서 쓴다. 삭제·숨김과 관계없이 읽고, 할 수 있는지는 Comment가 판단한다.
	// (COM-CMT-02, COM-CMT-04, COM-CMT-05)
	Optional<Comment> findByUuid(UUID uuid);

	// 다건 조회: 댓글 목록 한 페이지의 원댓글(깊이 0)을 읽는다. 정렬은 부르는 쪽이 Pageable로 넘긴다. (COM-CMT-03, COM-CMT-09)
	// 삭제·숨김 원댓글은 삭제·숨김되지 않은 답글(깊이 1 또는 2)이 하나라도 있을 때만 읽는다. 없으면 목록에서 빠진다. (COM-CMT-06)
	// 그래서 페이지 개수(totalElements)도 목록에 나오는 원댓글 수와 같다.
	@Query("""
		select c from Comment c
		where c.postId = :postId
			and c.depth = 0
			and ((c.deleted = false and c.hidden = false)
				or exists (
					select r.id from Comment r
					where r.postId = c.postId
						and r.deleted = false
						and r.hidden = false
						and (r.parentId = c.id
							or r.parentId in (select m.id from Comment m where m.parentId = c.id))))
		""")
	Page<Comment> findRootsInList(@Param("postId") Long postId, Pageable pageable);

	// 다건 조회: 댓글 목록에서 원댓글 한 페이지의 답글(깊이 1, 2)을 한 번의 쿼리로 읽는다. 원댓글마다 따로 읽지 않으려고 쓴다.
	// 깊이 2 답글은 부모가 깊이 1이라 하위 쿼리로 깊이 1 댓글의 id를 모아 함께 읽는다. 삭제·숨김 답글도 읽는다. (COM-CMT-03, COM-CMT-06)
	@Query("""
		select c from Comment c
		where c.parentId in :rootIds
			or c.parentId in (select m.id from Comment m where m.parentId in :rootIds)
		order by c.createdAt asc, c.id asc
		""")
	List<Comment> findRepliesOfRoots(@Param("rootIds") Collection<Long> rootIds);
}
