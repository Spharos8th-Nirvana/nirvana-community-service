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

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.nirvana.community.domain.post.stats.document.PostStats;
import com.nirvana.community.domain.post.stats.dto.PostCounts;
import com.nirvana.community.domain.post.stats.repository.PostStatsRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 게시글 집계 수를 MongoDB에서 읽는다. 문서가 없는 글은 PostCounts.ZERO를 준다. (COM-PST-21, COM-PST-23)
// 집계 수는 부가 정보다. MongoDB를 읽다가 오류가 나면 경고 로그를 남기고 0으로 대신 준다(목록·상세가 500이 되지 않게).
@Slf4j
@Service
@RequiredArgsConstructor
public class PostStatsReader {

	private final PostStatsRepository postStatsRepository;

	// 단건 조회: 글 하나의 집계 수를 읽는다. 게시글 상세에서 쓴다. 문서가 없으면 모두 0이다.
	public PostCounts getCounts(UUID postUuid) {
		try {
			return postStatsRepository.findById(postUuid.toString())
				.map(PostCounts::from)
				.orElse(PostCounts.ZERO);
		} catch (DataAccessException e) {
			log.warn("게시글 집계 수 단건 조회 실패, 0으로 대신 준다. postUuid={}", postUuid, e);
			return PostCounts.ZERO;
		}
	}

	// 다건 조회: 글 여러 개의 집계 수를 한 번의 쿼리($in)로 읽는다. 게시글 목록에서 쓴다.
	// 요청한 글 UUID마다 값이 하나씩 들어 있고(요청 순서 유지), 문서가 없는 글은 모두 0이다.
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

	// 다건 조회: 글 UUID 여러 개로 문서를 한 번에 찾아 글 UUID별 Map으로 만든다. 조회에 실패하면 빈 Map을 준다.
	private Map<String, PostStats> findStatsByPostUuid(Set<UUID> postUuids) {
		List<String> ids = postUuids.stream().map(UUID::toString).toList();
		try {
			return postStatsRepository.findAllById(ids).stream()
				.collect(Collectors.toMap(PostStats::postUuid, Function.identity()));
		} catch (DataAccessException e) {
			log.warn("게시글 집계 수 다건 조회 실패, 0으로 대신 준다. 글 수={}", ids.size(), e);
			return Map.of();
		}
	}
}
