package com.nirvana.community.domain.post.stats.service;

import java.util.OptionalLong;
import java.util.UUID;

import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.nirvana.community.domain.post.stats.document.PostStats;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 게시글 집계 수를 MongoDB에 쓴다. 읽고 +1 해서 저장하지 않고 $inc로 바꿔 동시 요청에도 수가 유실되지 않게 한다.
// 원장은 MySQL이라 부르는 쪽은 MySQL 트랜잭션이 커밋된 뒤에 부른다. 이미 커밋된 좋아요는 되돌리지 않는다.
// 오류가 나면 경고 로그를 남기고 빈 값을 준다. 0과 구분해서, 부르는 쪽이 MySQL에서 다시 세게 한다.
// (COM-LIK-01, COM-LIK-02, COM-LIK-05)
@Slf4j
@Service
@RequiredArgsConstructor
public class PostStatsWriter {

	private static final String ID = "_id";
	private static final String VIEW_COUNT = "viewCount";
	private static final String LIKE_COUNT = "likeCount";
	private static final String COMMENT_COUNT = "commentCount";
	private static final String UPDATED_AT = "updatedAt";

	private final MongoTemplate mongoTemplate;

	// 좋아요 수를 1 올리고 올린 뒤의 값을 준다.
	public OptionalLong increaseLikeCount(UUID postUuid) {
		return changeLikeCount(postUuid, 1);
	}

	// 좋아요 수를 1 내리고 내린 뒤의 값을 준다.
	public OptionalLong decreaseLikeCount(UUID postUuid) {
		return changeLikeCount(postUuid, -1);
	}

	// 문서가 없으면 다른 수는 0으로 해서 만든다(upsert). 올리기와 내리기 모두 조건 없이 더한다.
	// "0보다 클 때만 내린다" 같은 조건을 두면, 좋아요의 +1보다 취소의 -1이 먼저 도착했을 때 -1이 버려져 수가 1 남는다.
	// 조건 없이 더하면 도착 순서가 바뀌어도 합이 같다. 그 사이 저장값이 잠깐 음수일 수 있어 돌려줄 때는 0으로 올린다.
	private OptionalLong changeLikeCount(UUID postUuid, int delta) {
		Query query = Query.query(Criteria.where(ID).is(postUuid.toString()));
		Update update = new Update()
			.inc(LIKE_COUNT, (long) delta)
			.setOnInsert(VIEW_COUNT, 0L)
			.setOnInsert(COMMENT_COUNT, 0L)
			.currentDate(UPDATED_AT);
		FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true).upsert(true);
		try {
			PostStats changed = mongoTemplate.findAndModify(query, update, options, PostStats.class);
			return changed == null ? OptionalLong.empty() : OptionalLong.of(Math.max(0L, changed.likeCount()));
		} catch (DataAccessException e) {
			log.warn("게시글 좋아요 수 반영 실패. postUuid={}, delta={}", postUuid, delta, e);
			return OptionalLong.empty();
		}
	}
}
