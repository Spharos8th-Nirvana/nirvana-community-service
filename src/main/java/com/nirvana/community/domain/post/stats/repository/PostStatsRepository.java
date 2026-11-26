package com.nirvana.community.domain.post.stats.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.nirvana.community.domain.post.stats.document.PostStats;

public interface PostStatsRepository extends MongoRepository<PostStats, String> {
}
