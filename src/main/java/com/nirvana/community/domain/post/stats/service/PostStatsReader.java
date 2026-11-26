package com.nirvana.community.domain.post.stats.service;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.nirvana.community.domain.post.stats.document.PostStats;
import com.nirvana.community.domain.post.stats.dto.PostCounts;
import com.nirvana.community.domain.post.stats.repository.PostStatsRepository;

import lombok.RequiredArgsConstructor;

// 게시글 집계 수를 MongoDB에서 읽는다. 문서가 없는 글은 PostCounts.ZERO를 준다. (COM-PST-21, COM-PST-23)
@Service
@RequiredArgsConstructor
public class PostStatsReader {

	private final PostStatsRepository postStatsRepository;

	public PostCounts getCounts(UUID postUuid) {
		return postStatsRepository.findById(postUuid.toString())
			.map(PostCounts::from)
			.orElse(PostCounts.ZERO);
	}

	// 요청한 글 UUID마다 값이 하나씩 들어 있다(요청 순서 유지). 조회는 한 번의 쿼리($in)로 한다.
	public Map<UUID, PostCounts> getCountsByPostUuids(Collection<UUID> postUuids) {
		Set<UUID> uniquePostUuids = new LinkedHashSet<>(postUuids);
		if (uniquePostUuids.isEmpty()) {
			return Map.of();
		}
		Map<String, PostStats> statsByPostUuid = findStatsByPostUuid(uniquePostUuids);
		Map<UUID, PostCounts> countsByPostUuid = new LinkedHashMap<>();
		for (UUID postUuid : uniquePostUuids) {
			PostStats postStats = statsByPostUuid.get(postUuid.toString());
			countsByPostUuid.put(postUuid, postStats == null ? PostCounts.ZERO : PostCounts.from(postStats));
		}
		return countsByPostUuid;
	}

	private Map<String, PostStats> findStatsByPostUuid(Set<UUID> postUuids) {
		List<String> ids = postUuids.stream().map(UUID::toString).toList();
		return postStatsRepository.findAllById(ids).stream()
			.collect(Collectors.toMap(PostStats::postUuid, Function.identity()));
	}
}
