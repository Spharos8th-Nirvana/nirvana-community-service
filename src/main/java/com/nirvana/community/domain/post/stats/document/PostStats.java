package com.nirvana.community.domain.post.stats.document;

import java.time.Instant;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

// 화면에 보여 주는 게시글 집계 수. 원장은 MySQL이고 이 문서는 읽기용이다. (COM-PST-21, COM-PST-23, COM-LIK-10)
// _id는 MySQL posts.uuid와 같은 값이다. UUID 타입으로 두면 BSON 바이너리로 저장될 수 있어 36자 문자열로 고정한다.
@Document(collection = "postStats")
public record PostStats(
	@MongoId(FieldType.STRING) String postUuid,
	long viewCount,
	long likeCount,
	long commentCount,
	Instant updatedAt
) {
}
